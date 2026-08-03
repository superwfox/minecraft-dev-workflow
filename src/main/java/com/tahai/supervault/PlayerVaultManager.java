package com.tahai.supervault;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerVaultManager {

    private final Plugin plugin;
    private final File dataFile;
    private final YamlConfiguration config;
    private final Map<UUID, PlayerVault> vaults = new HashMap<UUID, PlayerVault>();

    public PlayerVaultManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not create data.yml: " + e.getMessage());
        }
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public PlayerVault loadVault(UUID uuid) {
        PlayerVault cached = vaults.get(uuid);
        if (cached != null) {
            return cached;
        }
        PlayerVault vault = new PlayerVault();
        List<?> list = config.getList("vaults." + uuid.toString());
        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof Map)) {
                    continue;
                }
                Map<?, ?> map = (Map<?, ?>) obj;
                Object typeObj = map.get("type");
                Object amountObj = map.get("amount");
                if (typeObj == null || amountObj == null || !(amountObj instanceof Number)) {
                    continue;
                }
                Material material = Material.matchMaterial(typeObj.toString());
                if (material == null) {
                    continue;
                }
                vault.getItems().add(new StoredItem(material, ((Number) amountObj).intValue()));
            }
        }
        vaults.put(uuid, vault);
        return vault;
    }

    public void saveVault(UUID uuid) {
        PlayerVault vault = vaults.get(uuid);
        if (vault == null) {
            return;
        }
        String path = "vaults." + uuid.toString();
        config.set(path, null);
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (StoredItem item : vault.getItems()) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("type", item.getMaterial().name());
            map.put("amount", item.getAmount());
            items.add(map);
        }
        config.set(path, items);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save data.yml: " + e.getMessage());
        }
    }

    public PlayerVault getVault(UUID uuid) {
        return loadVault(uuid);
    }

    public void save() {
        for (UUID uuid : new ArrayList<UUID>(vaults.keySet())) {
            saveVault(uuid);
        }
    }

    public void shutdown() {
        save();
    }

    public static class StoredItem {
        private final Material material;
        private final int amount;

        public StoredItem(Material material, int amount) {
            this.material = material;
            this.amount = amount;
        }

        public Material getMaterial() {
            return material;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class PlayerVault {
        private final List<StoredItem> items = new ArrayList<StoredItem>();

        public List<StoredItem> getItems() {
            return items;
        }
    }
}