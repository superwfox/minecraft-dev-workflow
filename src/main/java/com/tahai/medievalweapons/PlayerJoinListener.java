package com.tahai.medievalweapons;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileConfiguration config = event.getPlayer().getServer().getPluginManager()
                .getPlugin("MedievalWeapons").getConfig();
        String url = config.getString("resource-pack-url", "");
        if (!url.isEmpty()) {
            Player player = event.getPlayer();
            player.setResourcePack(url);
        }
    }
}