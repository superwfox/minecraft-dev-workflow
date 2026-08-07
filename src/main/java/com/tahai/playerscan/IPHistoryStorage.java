package com.tahai.playerscan;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class IPHistoryStorage {
    private final Plugin plugin;
    private final File dataFile;
    private final Map<String, PlayerRecord> records = new HashMap<>();

    public IPHistoryStorage(Plugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "ip_history.yml");
        load();
    }

    public Map<String, PlayerRecord> getRecords() {
        return records;
    }

    public void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        });
    }

    private void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = cfg.getConfigurationSection("players");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name", "");
            String ip = section.getString(key + ".ip", "");
            long lastSeen = section.getLong(key + ".lastSeen", 0L);
            records.put(key, new PlayerRecord(name, ip, lastSeen));
        }
    }

    private void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<String, PlayerRecord> entry : records.entrySet()) {
            PlayerRecord record = entry.getValue();
            cfg.set("players." + entry.getKey() + ".name", record.getName());
            cfg.set("players." + entry.getKey() + ".ip", record.getIp());
            cfg.set("players." + entry.getKey() + ".lastSeen", record.getLastSeen());
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class PlayerRecord {
        private final String name;
        private final String ip;
        private final long lastSeen;

        public PlayerRecord(String name, String ip, long lastSeen) {
            this.name = name;
            this.ip = ip;
            this.lastSeen = lastSeen;
        }

        public String getName() {
            return name;
        }

        public String getIp() {
            return ip;
        }

        public long getLastSeen() {
            return lastSeen;
        }
    }
}