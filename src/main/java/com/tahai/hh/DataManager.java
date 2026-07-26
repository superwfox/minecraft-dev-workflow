package com.tahai.hh;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class DataManager {
    private final Plugin plugin;
    private final File dataFile;
    private final YamlConfiguration pointsConfig;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "points.yml");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.pointsConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public int getPoints(Player player) {
        return pointsConfig.getInt(player.getUniqueId().toString(), 0);
    }

    public void addPoints(Player player, int amount) {
        String uuid = player.getUniqueId().toString();
        int current = pointsConfig.getInt(uuid, 0);
        pointsConfig.set(uuid, current + amount);
    }

    public void save() {
        try {
            pointsConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save points.yml", e);
        }
    }
}