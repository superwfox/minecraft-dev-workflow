package com.tahai.baoshi;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {
    private final Plugin plugin;
    private final File dataFile;
    private final YamlConfiguration config;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "gemdata.yml");
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public String getGemType(UUID uuid) {
        return config.getString(uuid.toString() + ".type");
    }

    public int getGemLevel(UUID uuid) {
        return config.getInt(uuid.toString() + ".level");
    }

    public void setGemData(UUID uuid, String type, int level) {
        config.set(uuid.toString() + ".type", type);
        config.set(uuid.toString() + ".level", level);
    }

    public void save() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save gemdata.yml: " + e.getMessage());
        }
    }

    public void shutdown() {
        save();
    }
}