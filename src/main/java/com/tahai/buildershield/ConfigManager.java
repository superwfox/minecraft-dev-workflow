package com.tahai.buildershield;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;

    public ConfigManager() {
        this.plugin = Bukkit.getPluginManager().getPlugin("BuilderShield");
        if (plugin == null) {
            throw new IllegalStateException("BuilderShield plugin not found");
        }
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void save() {
        plugin.saveConfig();
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public double getShieldRadius() {
        return config.getDouble("shield.radius");
    }

    public boolean isMobKnockbackEnabled() {
        return config.getBoolean("mobs.knockback");
    }

    public double getMobKnockbackPower() {
        return config.getDouble("mobs.knockback-power");
    }

    public boolean isArrowDeflectEnabled() {
        return config.getBoolean("arrows.deflect");
    }

    public String getShieldItem() {
        return config.getString("items.shield");
    }

    public String getMessagePlaced() {
        return config.getString("messages.placed");
    }

    public String getMessageRemoved() {
        return config.getString("messages.removed");
    }

    public String getMessageNoPermission() {
        return config.getString("messages.no-permission");
    }
}