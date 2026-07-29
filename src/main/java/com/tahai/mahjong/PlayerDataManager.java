package com.tahai.mahjong;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final Plugin plugin;
    private final File dataFile;
    private final YamlConfiguration config;
    private final Map<UUID, Integer> scores;

    public PlayerDataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.config = YamlConfiguration.loadConfiguration(dataFile);
        this.scores = new HashMap<>();
        load();
    }

    private void load() {
        scores.clear();
        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            scores.put(uuid, config.getInt(key, 0));
        }
    }

    public int getScore(Player player) {
        return scores.getOrDefault(player.getUniqueId(), 0);
    }

    public void setScore(Player player, int score) {
        scores.put(player.getUniqueId(), score);
    }

    public void addScore(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        scores.put(uuid, getScore(player) + amount);
    }

    public void subtractScore(Player player, int amount) {
        addScore(player, -amount);
    }

    public void save() {
        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }
        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save playerdata.yml: " + e.getMessage());
        }
    }

    public void shutdown() {
        // no-op, save is triggered by Main.onDisable
    }
}