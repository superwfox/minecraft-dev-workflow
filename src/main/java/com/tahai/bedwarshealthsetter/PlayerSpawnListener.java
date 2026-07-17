package com.tahai.bedwarshealthsetter;

import com.andrei1058.bedwars.api.events.player.PlayerSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerSpawn(PlayerSpawnEvent event) {
        Player player = event.getPlayer();
        player.setMaxHealth(40.0);
        player.setHealth(40.0);
    }
}