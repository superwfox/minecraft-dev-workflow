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

        AttributeModifier modifier = new AttributeModifier(slowKey, -0.25, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        AttributeInstance attribute = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attribute != null) {
            removeSlowModifiers(attribute);
            attribute.addModifier(modifier);
        }

        sessions.put(targetId, new Session(fisher.getUniqueId(), hook, modifier));
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
            FishHook hook = session.hook;

            if (target == null || fisher == null || !target.isOnline() || !fisher.isOnline()) {
                endSession(targetId);
                continue;
            }
            if (hook == null || !hook.isValid()) {
                endSession(targetId);
                continue;
            }
            Entity hooked = hook.getHookedEntity();
            if (!(hooked instanceof Player) || !hooked.getUniqueId().equals(targetId)) {
                endSession(targetId);
                continue;
            }
            if (hook.getWorld() != target.getWorld()) {
                endSession(targetId);
                continue;
            }
            if (target.isDead()) {
                endSession(targetId);
                continue;
            }

            Vector direction = fisher.getLocation().toVector().subtract(target.getLocation().toVector());
            direction.setY(0);
            double distance = direction.length();
            if (distance < 1.0E-4) {
                continue;
            }
            direction.normalize();

            double strength = Math.min(0.30, 0.10 * (0.5 + distance / 8.0));
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

        private Session(UUID fisherId, FishHook hook, AttributeModifier modifier) {
            this.fisherId = fisherId;
            this.hook = hook;
            this.modifier = modifier;
        }
    }
}