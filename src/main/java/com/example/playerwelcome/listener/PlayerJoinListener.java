package com.example.playerwelcome.listener;

import com.example.playerwelcome.util.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;

public class PlayerJoinListener implements Listener {

    private final MessageUtil messageUtil;

    public PlayerJoinListener() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerWelcome");
        if (plugin == null) {
            throw new IllegalStateException("PlayerWelcome plugin not found!");
        }
        this.messageUtil = new MessageUtil();
        this.messageUtil.initialize();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        messageUtil.sendWelcomeMessage(event.getPlayer());
    }
}