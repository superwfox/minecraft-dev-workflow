package com.tahai.chatapp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class ChatTriggerListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ChatApp");
        if (plugin == null) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> ChatGUIHolder.openMainMenu(player));
    }
}