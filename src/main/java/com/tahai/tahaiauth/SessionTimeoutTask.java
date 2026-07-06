package com.tahai.tahaiauth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionTimeoutTask extends BukkitRunnable implements Listener {

    private final Map<UUID, Long> lastActive = new HashMap<>();
    private DataManager dataManager;
    private ConfigManager configManager;

    private DataManager getDataManager() {
        if (dataManager == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("TahaiAuth");
            dataManager = new DataManager(plugin);
        }
        return dataManager;
    }

    private ConfigManager getConfigManager() {
        if (configManager == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("TahaiAuth");
            configManager = new ConfigManager(plugin);
        }
        return configManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (getDataManager().isAuthenticated(uuid)) {
            lastActive.put(uuid, System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastActive.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (getDataManager().isAuthenticated(uuid)) {
            lastActive.put(uuid, System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (getDataManager().isAuthenticated(uuid)) {
            lastActive.put(uuid, System.currentTimeMillis());
        }
    }

    @Override
    public void run() {
        int sessionTimeout = getConfigManager().getSessionTimeout();
        long now = System.currentTimeMillis();
        long timeoutMillis = sessionTimeout * 1000L;

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!getDataManager().isAuthenticated(uuid)) continue;

            Long last = lastActive.get(uuid);
            if (last == null) {
                // 没有记录，则当作刚认证
                lastActive.put(uuid, now);
                continue;
            }
            if (now - last > timeoutMillis) {
                getDataManager().setAuthenticated(uuid, false);
                player.kickPlayer(ChatColor.RED + "会话超时，请重新登录");
                lastActive.remove(uuid);
            }
        }
    }
}