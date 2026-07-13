package com.tahai.containersidepanel.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public final class ModConfig {
    private static Plugin plugin;
    private static FileConfiguration config;

    private ModConfig() {}

    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public static void reload() {
        if (plugin != null) {
            plugin.reloadConfig();
            config = plugin.getConfig();
        }
    }

    public static void save() {
        if (plugin != null) {
            plugin.saveConfig();
        }
    }

    public static boolean isEnabled() {
        return config != null && config.getBoolean("enabled", true);
    }

    public static String getKeyBind() {
        return config != null ? config.getString("key-bind", "key.container_panel.toggle") : "key.container_panel.toggle";
    }

    public static int getLockSlot() {
        return config != null ? config.getInt("lock-slot", -1) : -1;
    }

    public static boolean isLockSlotEnabled() {
        return getLockSlot() >= 0;
    }
}