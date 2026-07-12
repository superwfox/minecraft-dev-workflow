package com.tahai.trollplugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReverseMoveListener implements Listener {

    private static final Set<Player> reversedPlayers = ConcurrentHashMap.newKeySet();

    public static void addPlayer(Player player) {
        reversedPlayers.add(player);
    }

    public static void removePlayer(Player player) {
        reversedPlayers.remove(player);
    }

    public static void removeAllPlayers() {
        reversedPlayers.clear();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!reversedPlayers.contains(player)) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }
        Vector reverse = from.toVector().subtract(to.toVector()).normalize().multiply(0.5);
        event.setCancelled(true);
        player.setVelocity(reverse);
    }
}