package com.tahai.scancheck;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final DataManager dataManager;

    public JoinListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        if (ip != null && !ip.isEmpty()) {
            dataManager.recordPlayerJoin(playerName, ip);
        }
    }
}