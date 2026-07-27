package com.tahai.hs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class StatsManager {
    private final Plugin plugin;
    private final Map<UUID, StatsData> statsMap = new HashMap<>();
    private final File dataFile;

    public StatsManager(Plugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "stats.yml");
        loadData();
    }

    public static class StatsData {
        private int recycleCount;
        private double totalCoins;

        public StatsData(int recycleCount, double totalCoins) {
            this.recycleCount = recycleCount;
            this.totalCoins = totalCoins;
        }

        public int getRecycleCount() {
            return recycleCount;
        }

        public void setRecycleCount(int recycleCount) {
            this.recycleCount = recycleCount;
        }

        public double getTotalCoins() {
            return totalCoins;
        }

        public void setTotalCoins(double totalCoins) {
            this.totalCoins = totalCoins;
        }
    }

    public StatsData getStats(Player player) {
        return statsMap.get(player.getUniqueId());
    }

    public void addRecycle(Player player, int count, double coins) {
        UUID uuid = player.getUniqueId();
        StatsData data = statsMap.get(uuid);
        if (data == null) {
            data = new StatsData(0, 0.0);
            statsMap.put(uuid, data);
        }
        data.setRecycleCount(data.getRecycleCount() + count);
        data.setTotalCoins(data.getTotalCoins() + coins);
        saveData();
    }

    public void saveData() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, StatsData> entry : statsMap.entrySet()) {
            String path = entry.getKey().toString();
            yaml.set(path + ".count", entry.getValue().getRecycleCount());
            yaml.set(path + ".coins", entry.getValue().getTotalCoins());
        }
        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save stats.yml: " + e.getMessage());
        }
    }

    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : yaml.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            int count = yaml.getInt(key + ".count", 0);
            double coins = yaml.getDouble(key + ".coins", 0.0);
            statsMap.put(uuid, new StatsData(count, coins));
        }
    }

    public void shutdown() {
        saveData();
    }
}