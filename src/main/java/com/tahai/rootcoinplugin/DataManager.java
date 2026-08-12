package com.tahai.rootcoinplugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {

    private final File playersFile;
    private final File landsFile;
    private final YamlConfiguration playersConfig;
    private final YamlConfiguration landsConfig;

    public DataManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("RootCoinPlugin");
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        playersFile = new File(dataFolder, "players.yml");
        landsFile = new File(dataFolder, "lands.yml");
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);
        landsConfig = YamlConfiguration.loadConfiguration(landsFile);
    }

    public void save() {
        try {
            playersConfig.save(playersFile);
            landsConfig.save(landsFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Could not save data", e);
        }
    }

    public boolean bindPlayer(UUID uuid, String qq, String ip) {
        String playerPath = "players." + uuid.toString();
        if (playersConfig.contains(playerPath)) {
            return false;
        }
        if (getQQCountForIP(ip) >= 5) {
            return false;
        }
        playersConfig.set(playerPath + ".qq", qq);
        playersConfig.set(playerPath + ".ip", ip);
        playersConfig.set(playerPath + ".balance", 0.0);
        return true;
    }

    public String getQQ(UUID uuid) {
        return playersConfig.getString("players." + uuid.toString() + ".qq");
    }

    public String getIP(UUID uuid) {
        return playersConfig.getString("players." + uuid.toString() + ".ip");
    }

    public int getQQCountForIP(String ip) {
        ConfigurationSection players = playersConfig.getConfigurationSection("players");
        if (players == null) {
            return 0;
        }
        Set<String> qqs = new HashSet<>();
        for (String uuid : players.getKeys(false)) {
            String node = "players." + uuid;
            if (ip.equals(playersConfig.getString(node + ".ip"))) {
                String qq = playersConfig.getString(node + ".qq");
                if (qq != null) {
                    qqs.add(qq);
                }
            }
        }
        return qqs.size();
    }

    public double getBalance(UUID uuid) {
        return playersConfig.getDouble("players." + uuid.toString() + ".balance", 0.0);
    }

    public void setBalance(UUID uuid, double balance) {
        playersConfig.set("players." + uuid.toString() + ".balance", balance);
    }

    public void addBalance(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean addLand(String name, UUID owner) {
        ConfigurationSection lands = landsConfig.getConfigurationSection("lands");
        if (lands == null) {
            lands = landsConfig.createSection("lands");
        }
        if (lands.contains(name)) {
            return false;
        }
        lands.set(name, owner.toString());
        return true;
    }

    public boolean removeLand(String name) {
        ConfigurationSection lands = landsConfig.getConfigurationSection("lands");
        if (lands == null || !lands.contains(name)) {
            return false;
        }
        lands.set(name, null);
        return true;
    }

    public boolean hasLand(String name) {
        ConfigurationSection lands = landsConfig.getConfigurationSection("lands");
        return lands != null && lands.contains(name);
    }

    public UUID getLandOwner(String name) {
        ConfigurationSection lands = landsConfig.getConfigurationSection("lands");
        if (lands == null) {
            return null;
        }
        String owner = lands.getString(name);
        if (owner == null) {
            return null;
        }
        return UUID.fromString(owner);
    }
}