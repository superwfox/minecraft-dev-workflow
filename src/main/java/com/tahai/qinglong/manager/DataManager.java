package com.tahai.qinglong.manager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DataManager {

    private final Plugin plugin;
    private final File dataFile;
    private YamlConfiguration config;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create data.yml", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public PlayerData loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = playerDataMap.get(uuid);
        if (data != null) return data;

        ConfigurationSection section = config.getConfigurationSection(uuid.toString());
        if (section == null) {
            data = new PlayerData();
            playerDataMap.put(uuid, data);
            return data;
        }

        data = new PlayerData();
        data.setLevel(section.getInt("level", 1));
        data.setExp(section.getInt("exp", 0));
        data.setQinglongCoins(section.getInt("qinglongCoins", 0));
        data.setUnlockedAnimals(section.getStringList("unlockedAnimals"));

        ConfigurationSection upgradeSection = section.getConfigurationSection("equipmentUpgrades");
        Map<String, Integer> upgrades = new HashMap<>();
        if (upgradeSection != null) {
            for (String key : upgradeSection.getKeys(false)) {
                upgrades.put(key, upgradeSection.getInt(key, 0));
            }
        }
        data.setEquipmentUpgrades(upgrades);
        playerDataMap.put(uuid, data);
        return data;
    }

    public void savePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        ConfigurationSection section = config.createSection(uuid.toString());
        section.set("level", data.getLevel());
        section.set("exp", data.getExp());
        section.set("qinglongCoins", data.getQinglongCoins());
        section.set("unlockedAnimals", data.getUnlockedAnimals());

        ConfigurationSection upgradeSection = section.createSection("equipmentUpgrades");
        for (Map.Entry<String, Integer> entry : data.getEquipmentUpgrades().entrySet()) {
            upgradeSection.set(entry.getKey(), entry.getValue());
        }

        saveConfig();
    }

    public void saveAll() {
        for (UUID uuid : playerDataMap.keySet()) {
            PlayerData data = playerDataMap.get(uuid);
            ConfigurationSection section = config.createSection(uuid.toString());
            section.set("level", data.getLevel());
            section.set("exp", data.getExp());
            section.set("qinglongCoins", data.getQinglongCoins());
            section.set("unlockedAnimals", data.getUnlockedAnimals());

            ConfigurationSection upgradeSection = section.createSection("equipmentUpgrades");
            for (Map.Entry<String, Integer> entry : data.getEquipmentUpgrades().entrySet()) {
                upgradeSection.set(entry.getKey(), entry.getValue());
            }
        }
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.yml", e);
        }
    }

    public static class PlayerData {
        private int level = 1;
        private int exp = 0;
        private int qinglongCoins = 0;
        private List<String> unlockedAnimals = new ArrayList<>();
        private Map<String, Integer> equipmentUpgrades = new HashMap<>();

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }

        public int getExp() { return exp; }
        public void setExp(int exp) { this.exp = exp; }

        public int getQinglongCoins() { return qinglongCoins; }
        public void setQinglongCoins(int qinglongCoins) { this.qinglongCoins = qinglongCoins; }

        public List<String> getUnlockedAnimals() { return unlockedAnimals; }
        public void setUnlockedAnimals(List<String> unlockedAnimals) { this.unlockedAnimals = unlockedAnimals; }

        public Map<String, Integer> getEquipmentUpgrades() { return equipmentUpgrades; }
        public void setEquipmentUpgrades(Map<String, Integer> equipmentUpgrades) { this.equipmentUpgrades = equipmentUpgrades; }
    }
}