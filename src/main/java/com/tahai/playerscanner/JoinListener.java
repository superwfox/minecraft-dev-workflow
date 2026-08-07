package com.tahai.playerscanner;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class JoinListener implements Listener {
    private final DataManager dataManager;

    public JoinListener() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerScanner");
        this.dataManager = new DataManager(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.updatePlayerLogin(player.getName(), player.getAddress().getHostString());
    }
}