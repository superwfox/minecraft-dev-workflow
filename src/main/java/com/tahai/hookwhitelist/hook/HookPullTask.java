package com.tahai.hookwhitelist.hook;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HookPullTask extends BukkitRunnable {

    /**
     * 拉拽会话的有效 tick 数。原版 FishingHook#retrieve 在 CAUGHT_ENTITY 事件结束后会立刻
     * discard() 浮标实体，因此不能以 hook.isValid() 作为会话生命周期依据，否则拉力链在第 2 tick
     * 就会断开。这里改为固定拉拽窗口：在窗口内持续按距离施加原版式速度脉冲，窗口结束或到达
     * 钓鱼者附近时再结束会话并移除减速修饰器。
     */
    private static final int MAX_PULL_TICKS = 25;

    /** 与原版“被拽到钓鱼者身边”一致的到达判定半径。 */
    private static final double ARRIVE_DISTANCE = 2.0D;

    /** 单 tick 水平速度脉冲上限（方块/tick）。 */
    private static final double MAX_PULL_STRENGTH = 0.40D;

    /** 与原版 pullEntity 同源的按距离缩放系数：0.1 × 距离。 */
    private static final double PULL_PER_BLOCK = 0.10D;

    private final Plugin plugin;
    private final NamespacedKey slowKey;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final Set<UUID> pendingRemovals = new HashSet<>();

    public HookPullTask(Plugin plugin) {
        this.plugin = plugin;
        this.slowKey = new NamespacedKey(plugin, "hook_pull_slow");
        Bukkit.getPluginManager().registerEvents(new CleanupListener(), plugin);
    }

    public void startSession(Player fisher, FishHook hook, Player target) {
        if (fisher == null || hook == null || target == null) {
            return;
        }
        UUID targetId = target.getUniqueId();
        endSession(targetId);
        pendingRemovals.remove(targetId);

        AttributeModifier modifier = new AttributeModifier(slowKey, -0.25D, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        AttributeInstance attribute = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attribute != null) {
            removeSlowModifiers(attribute);
            attribute.addModifier(modifier);
        }

        sessions.put(targetId, new Session(fisher.getUniqueId(), hook, modifier, MAX_PULL_TICKS));
    }

    public void endSession(UUID targetId) {
        if (targetId == null) {
            return;
        }
        Session session = sessions.remove(targetId);
        if (session == null) {
            return;
        }
        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            removeSlowModifier(target);
            pendingRemovals.remove(targetId);
        } else {
            pendingRemovals.add(targetId);
        }
    }

    @Override
    public void run() {
        clearPendingRemovals();

        for (Map.Entry<UUID, Session> entry : new ArrayList<>(sessions.entrySet())) {
            UUID targetId = entry.getKey();
            Session session = entry.getValue();

            Player target = Bukkit.getPlayer(targetId);
            Player fisher = Bukkit.getPlayer(session.fisherId);

            if (target == null || fisher == null || !target.isOnline() || !fisher.isOnline()) {
                endSession(targetId);
                continue;
            }
            if (target.isDead() || !target.getWorld().equals(fisher.getWorld())) {
                endSession(targetId);
                continue;
            }
            if (session.ticksLeft <= 0) {
                endSession(targetId);
                continue;
            }
            session.ticksLeft--;

            FishHook hook = session.hook;
            if (hook != null && hook.isValid()) {
                Entity hooked = hook.getHookedEntity();
                if (hooked != null && !hooked.getUniqueId().equals(targetId)) {
                    endSession(targetId);
                    continue;
                }
            }

            Vector direction = fisher.getLocation().toVector().subtract(target.getLocation().toVector());
            direction.setY(0);
            double distance = direction.length();
            if (distance <= ARRIVE_DISTANCE) {
                endSession(targetId);
                continue;
            }
            direction.normalize();

            double strength = Math.min(MAX_PULL_STRENGTH, PULL_PER_BLOCK * distance);
            if (strength <= 0.0D) {
                continue;
            }
            Vector velocity = target.getVelocity();
            target.setVelocity(new Vector(
                    direction.getX() * strength,
                    velocity.getY(),
                    direction.getZ() * strength
            ));
        }
    }

    private void clearPendingRemovals() {
        if (pendingRemovals.isEmpty()) {
            return;
        }
        for (UUID id : new ArrayList<>(pendingRemovals)) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                removeSlowModifier(player);
                pendingRemovals.remove(id);
            }
        }
    }

    private void endAllSessions() {
        for (UUID id : new ArrayList<>(sessions.keySet())) {
            endSession(id);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeSlowModifier(player);
        }
        sessions.clear();
        pendingRemovals.clear();
    }

    private void removeSlowModifier(Player player) {
        if (player == null) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attribute != null) {
            removeSlowModifiers(attribute);
        }
    }

    private void removeSlowModifiers(AttributeInstance attribute) {
        for (AttributeModifier modifier : new ArrayList<>(attribute.getModifiers())) {
            if (modifier != null && slowKey.equals(modifier.getKey())) {
                attribute.removeModifier(modifier);
            }
        }
    }

    private final class CleanupListener implements Listener {

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            UUID id = event.getPlayer().getUniqueId();
            Session session = sessions.remove(id);
            if (session != null) {
                removeSlowModifier(event.getPlayer());
            }
            pendingRemovals.remove(id);
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            UUID id = player.getUniqueId();
            removeSlowModifier(player);
            pendingRemovals.remove(id);
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin() == plugin) {
                endAllSessions();
            }
        }
    }

    private static final class Session {
        private final UUID fisherId;
        private final FishHook hook;
        private final AttributeModifier modifier;
        private int ticksLeft;

        private Session(UUID fisherId, FishHook hook, AttributeModifier modifier, int ticksLeft) {
            this.fisherId = fisherId;
            this.hook = hook;
            this.modifier = modifier;
            this.ticksLeft = ticksLeft;
        }
    }
}