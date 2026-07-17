package com.tahai.jiyueserverplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager {

    private static Plugin plugin;

    public static void init(Plugin pl) {
        plugin = pl;
        pl.saveDefaultConfig();
        reload();
    }

    public static void reload() {
        // No configuration keys to load for now
    }

    public static void save() {
        plugin.saveConfig();
    }
}