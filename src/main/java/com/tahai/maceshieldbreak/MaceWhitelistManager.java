package com.tahai.maceshieldbreak;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaceWhitelistManager {

    private final JavaPlugin plugin;
    private final File file;
    private final Set<String> players = new HashSet<>();

    public MaceWhitelistManager(JavaPlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.file = new File(plugin.getDataFolder(), "whitelist.yml");
        if (!file.exists()) {
            plugin.saveResource("whitelist.yml", false);
        }
        reload();
    }

    public boolean contains(String name) {
        for (String p : players) {
            if (p.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void add(String name) {
        players.add(name);
        save();
    }

    public void remove(String name) {
        players.removeIf(p -> p.equalsIgnoreCase(name));
        save();
    }

    public void save() {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("players", new ArrayList<>(players));
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存 whitelist.yml: " + e.getMessage());
        }
    }

    public void reload() {
        players.clear();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        List<String> list = cfg.getStringList("players");
        players.addAll(list);
    }
}