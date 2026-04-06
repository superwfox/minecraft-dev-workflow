package com.example.playerwelcome.util;

import com.example.playerwelcome.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MessageUtil {
    private static ConfigManager configManager;

    private MessageUtil() {
    }

    public static void initialize() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerWelcome");
        if (plugin != null) {
            configManager = new ConfigManager(plugin);
        }
    }

    public static void sendWelcomeMessage(Player player) {
        if (configManager == null) {
            return;
        }
        String message = configManager.getWelcomeMessage();
        if (message != null && !message.isEmpty()) {
            player.sendMessage(colorize(message));
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(colorize(message));
        }
    }

    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}