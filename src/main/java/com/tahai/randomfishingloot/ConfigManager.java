package com.tahai.randomfishingloot;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled");
    }

    public double getChance() {
        return config.getDouble("chance");
    }

    public int getEnchantMin() {
        return config.getInt("enchant-count.min");
    }

    public int getEnchantMax() {
        return config.getInt("enchant-count.max");
    }

    public List<String> getEquipmentPool() {
        return config.getStringList("equipment-pool");
    }
}