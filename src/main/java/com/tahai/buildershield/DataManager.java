package com.tahai.buildershield;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final File dataFile;
    private final Map<UUID, ShieldData> shields = new HashMap<>();

    public DataManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BuilderShield");
        plugin.getDataFolder().mkdirs();
        dataFile = new File(plugin.getDataFolder(), "players.yml");
        load();
    }

    private void load() {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        int currentDay = getCurrentDay();
        for (String key : cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean enabled = cfg.getBoolean(key + ".enabled");
                int openedDay = cfg.getInt(key + ".opened_day");
                if (enabled && openedDay < currentDay) {
                    enabled = false;
                }
                shields.put(uuid, new ShieldData(enabled, openedDay));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, ShieldData> entry : shields.entrySet()) {
            String key = entry.getKey().toString();
            cfg.set(key + ".enabled", entry.getValue().enabled);
            cfg.set(key + ".opened_day", entry.getValue().openedDay);
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        save();
    }

    public void openShield(UUID uuid) {
        shields.put(uuid, new ShieldData(true, getCurrentDay()));
    }

    public void closeShield(UUID uuid) {
        ShieldData data = shields.get(uuid);
        if (data != null) {
            data.enabled = false;
        } else {
            shields.put(uuid, new ShieldData(false, getCurrentDay()));
        }
    }

    public boolean isShieldEnabled(UUID uuid) {
        ShieldData data = shields.get(uuid);
        return data != null && data.enabled;
    }

    public int getOpenedDay(UUID uuid) {
        ShieldData data = shields.get(uuid);
        return data == null ? -1 : data.openedDay;
    }

    private int getCurrentDay() {
        return (int) (Bukkit.getWorlds().get(0).getFullTime() / 24000L);
    }

    private static class ShieldData {
        boolean enabled;
        int openedDay;

        ShieldData(boolean enabled, int openedDay) {
            this.enabled = enabled;
            this.openedDay = openedDay;
        }
    }
}