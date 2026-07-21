package com.tahai.lottery;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {
    private final Plugin plugin;
    private final Map<String, BoxData> boxes = new HashMap<>();

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml");
            }
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection boxesSection = config.getConfigurationSection("boxes");
        if (boxesSection == null) return;
        for (String id : boxesSection.getKeys(false)) {
            ConfigurationSection boxSection = boxesSection.getConfigurationSection(id);
            if (boxSection == null) continue;
            Map<String, Double> probabilities = new HashMap<>();
            ConfigurationSection probSection = boxSection.getConfigurationSection("probabilities");
            if (probSection != null) {
                for (String tier : probSection.getKeys(false)) {
                    probabilities.put(tier, probSection.getDouble(tier));
                }
            }
            Map<String, List<ItemStack>> rewards = new HashMap<>();
            ConfigurationSection rewardsSection = boxSection.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                for (String tier : rewardsSection.getKeys(false)) {
                    List<Map<?, ?>> maps = rewardsSection.getMapList(tier);
                    List<ItemStack> items = new ArrayList<>();
                    for (Map<?, ?> map : maps) {
                        //noinspection unchecked
                        items.add(ItemStack.deserialize((Map<String, Object>) map));
                    }
                    rewards.put(tier, items);
                }
            }
            boxes.put(id, new BoxData(id, probabilities, rewards));
        }
    }

    public void save() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        YamlConfiguration config = new YamlConfiguration();
        for (BoxData box : boxes.values()) {
            String path = "boxes." + box.getId();
            config.set(path + ".probabilities", box.getProbabilities());
            Map<String, List<Map<String, Object>>> serializedRewards = new HashMap<>();
            for (Map.Entry<String, List<ItemStack>> entry : box.getRewards().entrySet()) {
                List<Map<String, Object>> serializedItems = new ArrayList<>();
                for (ItemStack item : entry.getValue()) {
                    serializedItems.add(item.serialize());
                }
                serializedRewards.put(entry.getKey(), serializedItems);
            }
            config.set(path + ".rewards", serializedRewards);
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml");
        }
    }

    public void addBox(String id, Map<String, Double> probabilities, Map<String, List<ItemStack>> rewards) {
        boxes.put(id, new BoxData(id, probabilities, rewards));
        save();
    }

    public void removeBox(String id) {
        boxes.remove(id);
        save();
    }

    public BoxData getBox(String id) {
        return boxes.get(id);
    }

    public Set<String> getAllBoxIds() {
        return boxes.keySet();
    }

    public String getConfigMessage(String path) {
        return plugin.getConfig().getString(path, "");
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public static class BoxData {
        private final String id;
        private final Map<String, Double> probabilities;
        private final Map<String, List<ItemStack>> rewards;

        public BoxData(String id, Map<String, Double> probabilities, Map<String, List<ItemStack>> rewards) {
            this.id = id;
            this.probabilities = probabilities;
            this.rewards = rewards;
        }

        public String getId() {
            return id;
        }

        public Map<String, Double> getProbabilities() {
            return probabilities;
        }

        public Map<String, List<ItemStack>> getRewards() {
            return rewards;
        }
    }
}