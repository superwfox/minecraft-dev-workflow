package com.tahai.itemban;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataManager {

    private final Set<String> bannedNBTKeys = new HashSet<>();
    private final File dataFile;

    public DataManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemBan");
        if (plugin == null) {
            throw new IllegalStateException("ItemBan plugin not found");
        }
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "banned-nbt-keys.yml");
        load();
    }

    private void load() {
        bannedNBTKeys.clear();
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        bannedNBTKeys.addAll(cfg.getStringList("banned-nbt-keys"));
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("banned-nbt-keys", new ArrayList<>(bannedNBTKeys));
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        save();
    }

    public void reload() {
        load();
    }

    public boolean add(String key) {
        if (bannedNBTKeys.add(key)) {
            save();
            return true;
        }
        return false;
    }

    public boolean remove(String key) {
        if (bannedNBTKeys.remove(key)) {
            save();
            return true;
        }
        return false;
    }

    public List<String> list() {
        return new ArrayList<>(bannedNBTKeys);
    }

    public boolean contains(String key) {
        return bannedNBTKeys.contains(key);
    }
}