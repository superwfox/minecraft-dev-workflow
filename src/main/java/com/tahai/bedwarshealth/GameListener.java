package com.tahai.bedwarshealth;

import com.andrei1058.bedwars.api.arena.Arena;
import com.andrei1058.bedwars.api.events.game.GameStartedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class GameListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGameStart(GameStartedEvent event) {
        Arena arena = event.getArena();
        if (arena == null) return;
        for (Player player : arena.getPlayers()) {
            player.setMaxHealth(40.0);
            player.setHealth(40.0);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        player.setMaxHealth(40.0);
        player.setHealth(40.0);
    }
}