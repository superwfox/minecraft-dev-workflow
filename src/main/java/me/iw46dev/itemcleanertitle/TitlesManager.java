package me.iw46dev.itemcleanertitle;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

public class TitlesManager {

    private final Plugin plugin;
    private final File dataFile;
    private final Map<UUID, String> titles = new HashMap<>();

    public TitlesManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "titles.yml");
        load();
    }

    private void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String title = cfg.getString(key);
                if (title != null) {
                    titles.put(uuid, title);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void setTitle(UUID uuid, String title) {
        titles.put(uuid, title);
    }

    public void removeTitle(UUID uuid) {
        titles.remove(uuid);
    }

    public String getTitle(UUID uuid) {
        return titles.get(uuid);
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : titles.entrySet()) {
            cfg.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            cfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        save();
    }
}