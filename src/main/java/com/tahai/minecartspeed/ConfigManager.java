package com.tahai.minecartspeed;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ConfigManager {
    private static ConfigManager instance;
    private final Plugin plugin;

    private ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("MinecartSpeed");
            if (plugin != null) {
                instance = new ConfigManager(plugin);
            }
        }
        return instance;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public void save() {
        plugin.saveConfig();
    }

    public double getSpeedMin() {
        return plugin.getConfig().getDouble("speed-min");
    }

    public double getSpeedMax() {
        return plugin.getConfig().getDouble("speed-max");
    }

    public double getDefaultSpeed() {
        return plugin.getConfig().getDouble("default-speed");
    }

    public String getActionbarEnter() {
        return plugin.getConfig().getString("actionbar-enter", "");
    }

    public String getActionbarLeave() {
        return plugin.getConfig().getString("actionbar-leave", "");
    }

    public String getActionbarChange() {
        return plugin.getConfig().getString("actionbar-change", "");
    }

    public boolean isIgnoreY() {
        return plugin.getConfig().getBoolean("ignore-y", false);
    }
}