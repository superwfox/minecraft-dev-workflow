package com.tahai.scanmanager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

public class JoinListener implements Listener {

    private final DataManager dataManager;

    public JoinListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        InetSocketAddress address = player.getAddress();
        if (address == null) return;
        InetAddress inetAddress = address.getAddress();
        if (inetAddress == null) return;
        String ip = inetAddress.getHostAddress();
        dataManager.addMapping(uuid, ip);
    }
}