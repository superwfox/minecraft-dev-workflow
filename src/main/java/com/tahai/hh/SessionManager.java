package com.tahai.hh;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SessionManager {

    private final Set<UUID> activePlayers;
    private final Plugin plugin;

    public SessionManager(Plugin plugin) {
        this.plugin = plugin;
        this.activePlayers = new HashSet<>();
        // 通过服务管理器注册，避免静态单例
        plugin.getServer().getServicesManager().register(
                SessionManager.class, this, plugin, ServicePriority.Normal
        );
    }

    public void addPlayer(UUID uuid) {
        activePlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }

    public boolean isPlayerActive(UUID uuid) {
        return activePlayers.contains(uuid);
    }

    public void shutdown() {
        activePlayers.clear();
    }
}