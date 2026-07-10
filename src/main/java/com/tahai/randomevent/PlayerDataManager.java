package com.tahai.randomevent;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final Plugin plugin;
    private final Map<UUID, Integer> passCountMap = new HashMap<>();
    private final File dataFile;

    public PlayerDataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "player_passcount.txt");
        load();
    }

    private void load() {
        if (!dataFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(":", 2);
                if (parts.length != 2) continue;
                try {
                    UUID uuid = UUID.fromString(parts[0]);
                    int count = Integer.parseInt(parts[1]);
                    passCountMap.put(uuid, count);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load player pass count data: " + e.getMessage());
        }
    }

    public int getPassCount(UUID uuid) {
        return passCountMap.getOrDefault(uuid, 0);
    }

    public void addPassCount(UUID uuid) {
        passCountMap.merge(uuid, 1, Integer::sum);
    }

    public void save() {
        try {
            plugin.getDataFolder().mkdirs();
        } catch (Exception ignored) {
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            for (Map.Entry<UUID, Integer> entry : passCountMap.entrySet()) {
                writer.write(entry.getKey().toString() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player pass count data: " + e.getMessage());
        }
    }

    public void shutdown() {
        save();
    }
}