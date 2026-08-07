package com.tahai.scancheck;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DataManager {
    private final File dataFile;
    private final YamlConfiguration config;
    private final Plugin plugin;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "players.yml");
        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void recordPlayerJoin(String playerName, String ip) {
        String path = "players." + playerName;
        config.set(path + ".lastIp", ip);
        List<String> ips = config.getStringList(path + ".ips");
        if (!ips.contains(ip)) {
            ips.add(ip);
            config.set(path + ".ips", ips);
        }
    }

    public String getLastIp(String playerName) {
        return config.getString("players." + playerName + ".lastIp", null);
    }

    public List<String> findPlayersByLastIp(String ip) {
        List<String> result = new ArrayList<>();
        ConfigurationSection players = config.getConfigurationSection("players");
        if (players == null) {
            return result;
        }
        for (String name : players.getKeys(false)) {
            String lastIp = config.getString("players." + name + ".lastIp");
            if (ip.equals(lastIp)) {
                result.add(name);
            }
        }
        return result;
    }

    public List<String> findPlayersByIpHistory(String ip) {
        List<String> result = new ArrayList<>();
        ConfigurationSection players = config.getConfigurationSection("players");
        if (players == null) {
            return result;
        }
        for (String name : players.getKeys(false)) {
            List<String> ips = config.getStringList("players." + name + ".ips");
            if (ips.contains(ip)) {
                result.add(name);
            }
        }
        return result;
    }

    public void save() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save players.yml", e);
        }
    }
}