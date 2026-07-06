package com.tahai.tahaiauth;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AuthListener implements Listener {

    private final DataManager dataManager;
    private final ConfigManager configManager;

    public AuthListener(DataManager dataManager, ConfigManager configManager) {
        this.dataManager = dataManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (dataManager.isRegistered(player.getUniqueId())) {
            if (!dataManager.isAuthenticated(player.getUniqueId())) {
                player.sendMessage(ChatColor.GOLD + "请使用 /auth <密码> 登录");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "请使用 /auth <密码> 注册");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!dataManager.isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        } else {
            dataManager.updateLastActive(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!dataManager.isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        } else {
            dataManager.updateLastActive(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!dataManager.isAuthenticated(player.getUniqueId())) {
            String command = event.getMessage().toLowerCase().trim();
            if (command.startsWith("/auth") || command.startsWith("/auth ")) {
                return;
            }
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "你必须先登录才能使用此命令");
        } else {
            dataManager.updateLastActive(player.getUniqueId());
        }
    }
}