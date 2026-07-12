package com.tahai.weaponskills;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;

public class DataManager {

    private final Map<UUID, String> skillMap;
    private final File dataFile;

    public DataManager(Plugin plugin) {
        skillMap = new HashMap<>();
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        dataFile = new File(dataFolder, "weaponskills.txt");
        load();
    }

    private void load() {
        if (!dataFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    try {
                        UUID uuid = UUID.fromString(parts[0]);
                        String skill = parts[1];
                        skillMap.put(uuid, skill);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning("Could not load weaponskills.txt: " + e.getMessage());
        }
    }

    public String getSkill(UUID playerUUID) {
        return skillMap.get(playerUUID);
    }

    public void setSkill(UUID playerUUID, String skill) {
        if (skill == null) {
            skillMap.remove(playerUUID);
        } else {
            skillMap.put(playerUUID, skill);
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            for (Map.Entry<UUID, String> entry : skillMap.entrySet()) {
                writer.write(entry.getKey().toString() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning("Could not save weaponskills.txt: " + e.getMessage());
        }
    }

    public void shutdown() {
        save();
    }
}