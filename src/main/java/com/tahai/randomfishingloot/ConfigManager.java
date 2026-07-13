package com.tahai.randomfishingloot;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ConfigManager {

    private final Plugin plugin;
    private boolean enabled;
    private double chance;
    private int enchantCountMin;
    private int enchantCountMax;
    private List<String> equipmentPool;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        FileConfiguration config = plugin.getConfig();
        enabled = config.getBoolean("enabled", false);
        chance = config.getDouble("chance", 0.1);
        String enchantCountStr = config.getString("enchant-count", "1-3");
        String[] parts = enchantCountStr.split("-");
        try {
            enchantCountMin = Integer.parseInt(parts[0].trim());
            enchantCountMax = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : enchantCountMin;
        } catch (NumberFormatException e) {
            enchantCountMin = 1;
            enchantCountMax = 3;
        }
        equipmentPool = config.getStringList("equipment-pool");
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getChance() {
        return chance;
    }

    public int getEnchantCountMin() {
        return enchantCountMin;
    }

    public int getEnchantCountMax() {
        return enchantCountMax;
    }

    public List<String> getEquipmentPool() {
        return equipmentPool;
    }
}