package com.tahai.infinitewarehouse;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final Plugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<UUID, ItemStack[]> cache = new HashMap<>();

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public ItemStack[] getWarehouse(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack[] contents = cache.get(uuid);
        if (contents == null) {
            contents = load(player);
            cache.put(uuid, contents);
        }
        return contents;
    }

    public void saveWarehouse(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack[] contents = cache.get(uuid);
        if (contents == null) {
            contents = load(player);
            cache.put(uuid, contents);
        }
        saveToConfig(uuid, contents);
        saveFile();
    }

    public void saveAll() {
        for (Map.Entry<UUID, ItemStack[]> entry : cache.entrySet()) {
            saveToConfig(entry.getKey(), entry.getValue());
        }
        saveFile();
    }

    private ItemStack[] load(Player player) {
        ItemStack[] contents = new ItemStack[54];
        ConfigurationSection section = config.getConfigurationSection("warehouses." + player.getUniqueId().toString());
        if (section != null) {
            for (int i = 0; i < contents.length; i++) {
                ConfigurationSection itemSection = section.getConfigurationSection(String.valueOf(i));
                if (itemSection != null) {
                    contents[i] = ItemStack.deserialize(itemSection.getValues(false));
                }
            }
        }
        return contents;
    }

    private void saveToConfig(UUID uuid, ItemStack[] contents) {
        String path = "warehouses." + uuid.toString();
        config.set(path, null);
        ConfigurationSection section = config.createSection(path);
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null) {
                section.set(String.valueOf(i), item.serialize());
            }
        }
    }

    private void saveFile() {
        try {
            file.getParentFile().mkdirs();
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.yml: " + e.getMessage());
        }
    }
}