package com.tahai.onlyshop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataManager {
    private final Plugin plugin;
    private final File dataFile;
    private final YamlConfiguration dataConfig;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public List<String> getItemList() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("items");
        if (section == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(section.getKeys(false));
    }

    public double getPrice(String itemId) {
        return plugin.getConfig().getDouble("items." + itemId + ".price", 0.0);
    }

    public String getCurrency(String itemId) {
        return plugin.getConfig().getString("items." + itemId + ".currency", "coin");
    }

    public int getLimit(String itemId) {
        return plugin.getConfig().getInt("items." + itemId + ".limit", -1);
    }

    public void updateItem(String itemId, double price, String currency, int limit) {
        plugin.getConfig().set("items." + itemId + ".price", price);
        plugin.getConfig().set("items." + itemId + ".currency", currency);
        plugin.getConfig().set("items." + itemId + ".limit", limit);
        plugin.saveConfig();
    }

    public int getPurchaseCount(String uuid, String itemId) {
        return dataConfig.getInt("player-records." + uuid + "." + itemId, 0);
    }

    public void setPurchaseCount(String uuid, String itemId, int count) {
        dataConfig.set("player-records." + uuid + "." + itemId, count);
    }

    public void save() {
        plugin.saveConfig();
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save data.yml: " + e.getMessage());
        }
    }

    public void shutdown() {
        save();
    }
}