package com.tahai.healthadjuster;

import com.andrei1058.bedwars.api.events.gameplay.GameStartedEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;

public class HealthAdjustListener implements Listener {

    private final double health;

    public HealthAdjustListener(double health) {
        this.health = health;
    }

    @EventHandler
    public void onGameStarted(GameStartedEvent event) {
        Collection<Player> players = event.getArena().getPlayers();
        if (players == null) return;
        for (Player player : players) {
            player.setMaxHealth(health);
            player.setHealth(health);
        }
    }

    @EventHandler
    public void onPlayerReSpawn(PlayerReSpawnEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        player.setMaxHealth(health);
        player.setHealth(health);
    }
}