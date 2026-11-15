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

public class HookPullTask extends BukkitRunnable implements Listener {

    /**
     * 拉拽窗口的 tick 数。原版 FishingHook 在 CAUGHT_ENTITY 结束后会 discard() 浮标实体，
     * 因此钩子有效性不能当作“仍在拉拽”的判据，这里使用固定窗口来保证拉拽时长稳定。
     */
    private static final int MAX_PULL_TICKS = 25;

    /** 与原版“被拽到钓鱼者身边”一致的到达判定半径。 */
    private static final double ARRIVE_DISTANCE = 2.0D;

    /** 单 tick 水平速度脉冲上限（方块/tick）。 */
    private static final double MAX_PULL_STRENGTH = 0.45D;

    /** 与原版 pullEntity 同源的按距离缩放系数：0.1 × 距离。 */
    private static final double PULL_PER_BLOCK = 0.10D;

    /** 勾中瞬间的水平冲量上限，避免超远距离抛竿把玩家弹飞。 */
    private static final double MAX_IMPULSE = 1.2D;

    /** 垂直抬升上限，保证被拽轨迹与原版勾生物接近而不至于起飞。 */
    private static final double MAX_LIFT = 0.40D;

    private final Plugin plugin;
    private final NamespacedKey slowKey;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final Set<UUID> pendingRemovals = new HashSet<>();

    public HookPullTask(Plugin plugin) {
        this.plugin = plugin;
        this.slowKey = new NamespacedKey(plugin, "hook_pull_slow");
        Bukkit.getPluginManager().registerEvents(this, plugin);
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

        // 勾中瞬间立刻给出与原版 pullEntity 同源的一次冲量（含垂直抬升），
        // 这样即使后续会话被外部提前结束，被勾玩家也已经得到原版强度的拽动，手感不会忽有忽无。
        applyVanillaImpulse(target, fisher);

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

            FishHook hook = session.hook;
            if (hook != null && hook.isValid()) {
                Entity hooked = hook.getHookedEntity();
                if (hooked != null && !hooked.getUniqueId().equals(targetId)) {
                    endSession(targetId);
                    continue;
                }
            }

            session.ticksLeft--;

            Vector delta = fisher.getLocation().toVector().subtract(target.getLocation().toVector());
            double distance = delta.length();
            Vector horizontal = new Vector(delta.getX(), 0.0D, delta.getZ());
            double horizontalDistance = horizontal.length();
            if (horizontalDistance <= ARRIVE_DISTANCE) {
                endSession(targetId);
                continue;
            }
            horizontal.multiply(1.0D / horizontalDistance);

            double strength = Math.min(MAX_PULL_STRENGTH, PULL_PER_BLOCK * horizontalDistance);
            if (strength <= 0.0D) {
                continue;
            }

            Vector velocity = target.getVelocity();
            // 保留一半的原有水平动量（玩家自身移动、原版 pullEntity 冲量），
            // 再叠加朝钓鱼者的拉力，使拉拽自然而不是每 tick 硬性覆盖速度。
            double vx = velocity.getX() * 0.5D + horizontal.getX() * strength;
            double vz = velocity.getZ() * 0.5D + horizontal.getZ() * strength;

            // 垂直方向保留自然下落分量；贴地时按原版比例（0.1 × dY + sqrt(dist) × 0.08）给一次抬升，
            // 让被拽玩家能自然越上台阶、在高度差地形上不被地面卡住。
            double vy = velocity.getY();
            if (target.isOnGround()) {
                double lift = delta.getY() * PULL_PER_BLOCK + Math.sqrt(distance) * 0.08D;
                lift = Math.max(0.0D, Math.min(MAX_LIFT, lift));
                vy = Math.max(vy, lift);
            }

            target.setVelocity(new Vector(vx, vy, vz));
        }
    }

    /**
     * 与原版 FishingHook 勾中实体时一致的冲量：水平 0.1 × 距离，垂直 0.1 × dY + sqrt(距离) × 0.08，
     * 并叠加到玩家现有速度上，绝不使用传送。
     */
    private void applyVanillaImpulse(Player target, Player fisher) {
        Vector delta = fisher.getLocation().toVector().subtract(target.getLocation().toVector());
        double distance = delta.length();
        if (distance <= 0.0D) {
            return;
        }

        double impulseX = delta.getX() * PULL_PER_BLOCK;
        double impulseZ = delta.getZ() * PULL_PER_BLOCK;
        double horizontalLength = Math.sqrt(impulseX * impulseX + impulseZ * impulseZ);
        if (horizontalLength > MAX_IMPULSE && horizontalLength > 0.0D) {
            double scale = MAX_IMPULSE / horizontalLength;
            impulseX *= scale;
            impulseZ *= scale;
        }

        double lift = delta.getY() * PULL_PER_BLOCK + Math.sqrt(distance) * 0.08D;
        lift = Math.max(-MAX_LIFT, Math.min(MAX_LIFT, lift));

        Vector velocity = target.getVelocity();
        target.setVelocity(new Vector(
                velocity.getX() + impulseX,
                velocity.getY() + lift,
                velocity.getZ() + impulseZ
        ));
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