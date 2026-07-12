package com.tahai.prankplugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ReverseMoveListener implements Listener {

    private static final Set<UUID> reversePlayers = new HashSet<>();

    public static void addReversePlayer(UUID playerId) {
        reversePlayers.add(playerId);
    }

    public static void removeReversePlayer(UUID playerId) {
        reversePlayers.remove(playerId);
    }

    public static boolean isReversePlayer(UUID playerId) {
        return reversePlayers.contains(playerId);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!reversePlayers.contains(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from == null || to == null || !from.getWorld().equals(to.getWorld())) {
            return;
        }

        // Reverse horizontal movement direction
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        Location reversed = from.clone();
        reversed.setX(from.getX() - dx);
        reversed.setZ(from.getZ() - dz);
        // Keep yaw/pitch as is (player rotation unchanged)
        reversed.setYaw(to.getYaw());
        reversed.setPitch(to.getPitch());

        event.setTo(reversed);
    }
}