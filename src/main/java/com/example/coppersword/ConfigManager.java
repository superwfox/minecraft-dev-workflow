package com.example.coppersword;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;
    private YamlConfiguration messages;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadMessages();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadMessages();
    }

    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public int getStarRangeMin() {
        return config.getInt("star-range.min");
    }

    public int getStarRangeMax() {
        return config.getInt("star-range.max");
    }

    public int getCooldownSeconds() {
        return config.getInt("cooldown-seconds");
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(path, ""));
    }
}