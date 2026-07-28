package com.tahai.kuangqu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class DataManager {

    private final JavaPlugin plugin;

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public ConfigurationSection getAllZones() {
        return getConfig().getConfigurationSection("zones");
    }

    public ConfigurationSection getZone(String name) {
        return getConfig().getConfigurationSection("zones." + name);
    }

    public void addZone(String name, Map<String, Object> data) {
        getConfig().set("zones." + name, data);
        saveConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }
}