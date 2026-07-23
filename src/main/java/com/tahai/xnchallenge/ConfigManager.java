package com.tahai.xnchallenge;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public List<String> getAllowedWorlds() {
        return config.getStringList("allowed-worlds");
    }

    public int getCooldownSeconds() {
        return config.getInt("cooldown-seconds", 60);
    }

    public List<Wave> getWaves() {
        List<Wave> waves = new ArrayList<>();
        ConfigurationSection wavesSection = config.getConfigurationSection("waves");
        if (wavesSection == null) {
            plugin.getLogger().warning("No 'waves' section found in config.yml");
            return waves;
        }
        for (String key : wavesSection.getKeys(false)) {
            ConfigurationSection waveSection = wavesSection.getConfigurationSection(key);
            if (waveSection == null) continue;
            int waveNumber = waveSection.getInt("wave-number", 0);
            List<MonsterSpawn> mobs = new ArrayList<>();
            List<?> mobsList = waveSection.getList("mobs");
            if (mobsList != null) {
                for (Object obj : mobsList) {
                    if (obj instanceof ConfigurationSection) {
                        ConfigurationSection mobSection = (ConfigurationSection) obj;
                        String type = mobSection.getString("type", "ZOMBIE");
                        int amount = mobSection.getInt("amount", 1);
                        int delay = mobSection.getInt("delay", 0);
                        mobs.add(new MonsterSpawn(type, amount, delay));
                    }
                }
            }
            List<String> rewards = waveSection.getStringList("rewards");
            waves.add(new Wave(waveNumber, mobs, rewards));
        }
        return waves;
    }

    public List<String> getFinalRewards() {
        return config.getStringList("rewards");
    }

    public static class Wave {
        private final int waveNumber;
        private final List<MonsterSpawn> mobs;
        private final List<String> rewards;

        public Wave(int waveNumber, List<MonsterSpawn> mobs, List<String> rewards) {
            this.waveNumber = waveNumber;
            this.mobs = mobs;
            this.rewards = rewards;
        }

        public int getWaveNumber() { return waveNumber; }
        public List<MonsterSpawn> getMobs() { return mobs; }
        public List<String> getRewards() { return rewards; }
    }

    public static class MonsterSpawn {
        private final String type;
        private final int amount;
        private final int delay;

        public MonsterSpawn(String type, int amount, int delay) {
            this.type = type;
            this.amount = amount;
            this.delay = delay;
        }

        public String getType() { return type; }
        public int getAmount() { return amount; }
        public int getDelay() { return delay; }
    }
}