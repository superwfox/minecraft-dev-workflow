package com.tahai.hookwhitelist.whitelist;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistManager {

    private final Plugin plugin;
    private final File dataFile;
    private final Map<UUID, String> entries = new LinkedHashMap<>();

    public WhitelistManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "whitelist.yml");
    }

    public void load() {
        entries.clear();
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("无法创建插件数据目录");
        }
        if (!dataFile.exists()) {
            save();
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = cfg.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            String name = section.getString(key);
            if (name == null) {
                continue;
            }
            try {
                entries.put(UUID.fromString(key), name);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("无法创建插件数据目录");
            return;
        }
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : entries.entrySet()) {
            cfg.set("players." + entry.getKey(), entry.getValue());
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("保存 whitelist.yml 失败: " + e.getMessage());
        }
    }

    public boolean contains(UUID uuid) {
        return entries.containsKey(uuid);
    }

    public boolean add(UUID uuid, String name) {
        if (uuid == null || name == null) {
            return false;
        }
        boolean changed = !name.equals(entries.put(uuid, name));
        if (changed) {
            save();
        }
        return changed;
    }

    public boolean remove(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        boolean removed = entries.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public List<String> getNames() {
        return new ArrayList<>(entries.values());
    }
}