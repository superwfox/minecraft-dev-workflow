package com.tahai.maceshieldbreaker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.plugin.Plugin;

public class WhitelistManager {

    private final Plugin plugin;
    private final File file;
    private final Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public WhitelistManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "whitelist.txt");
        load();
    }

    public void load() {
        names.clear();
        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("无法创建 whitelist.txt: " + e.getMessage());
            }
            return;
        }
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                names.add(trimmed);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("无法读取 whitelist.txt: " + e.getMessage());
        }
    }

    public void save() {
        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            Files.write(file.toPath(), names, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存 whitelist.txt: " + e.getMessage());
        }
    }

    public boolean add(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (names.add(trimmed)) {
            save();
            return true;
        }
        return false;
    }

    public boolean remove(String name) {
        if (name == null) {
            return false;
        }
        if (names.remove(name.trim())) {
            save();
            return true;
        }
        return false;
    }

    public boolean isWhitelisted(String name) {
        if (name == null) {
            return false;
        }
        return names.contains(name.trim());
    }

    public List<String> getNames() {
        return new ArrayList<>(names);
    }
}