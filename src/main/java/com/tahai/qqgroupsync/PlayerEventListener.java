package com.tahai.qqgroupsync;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        String message = playerName + " 来了！";
        try {
            long groupId = Long.parseLong(ConfigManager.GroupId);
            OneBotApi.sendG(groupId, message);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("[QQGroupSync] Invalid group ID in config: " + ConfigManager.GroupId);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        String message = playerName + " 离开了。";
        try {
            long groupId = Long.parseLong(ConfigManager.GroupId);
            OneBotApi.sendG(groupId, message);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("[QQGroupSync] Invalid group ID in config: " + ConfigManager.GroupId);
        }
    }
}