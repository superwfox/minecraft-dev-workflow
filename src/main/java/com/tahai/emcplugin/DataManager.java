package com.tahai.emcplugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataManager {

    private final Plugin plugin;
    private final Map<String, Double> values = new HashMap<>();
    private final Map<UUID, Double> points = new HashMap<>();

    public DataManager() {
        this.plugin = Bukkit.getPluginManager().getPlugin("EMCPlugin");
        if (this.plugin == null) {
            throw new IllegalStateException("EMCPlugin not found");
        }
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File valuesFile = new File(plugin.getDataFolder(), "values.yml");
        if (!valuesFile.exists()) {
            ((JavaPlugin) plugin).saveResource("values.yml", false);
        }
        loadValues();
        loadPoints();
    }

    public void loadValues() {
        values.clear();
        File file = new File(plugin.getDataFolder(), "values.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            values.put(key, cfg.getDouble(key));
        }
    }

    public double getValue(String itemName) {
        return values.getOrDefault(itemName, 0.0);
    }

    public double getPoints(UUID playerId) {
        return points.getOrDefault(playerId, 0.0);
    }

    public void addPoints(UUID playerId, double amount) {
        points.put(playerId, getPoints(playerId) + amount);
    }

    public boolean removePoints(UUID playerId, double amount) {
        double current = getPoints(playerId);
        if (current < amount) {
            return false;
        }
        points.put(playerId, current - amount);
        return true;
    }

    public void loadPoints() {
        points.clear();
        File file = new File(plugin.getDataFolder(), "points.json");
        if (!file.exists()) {
            return;
        }
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Matcher matcher = Pattern.compile("\"([0-9a-fA-F-]+)\"\\s*:\\s*([0-9.]+(?:[eE][+-]?[0-9]+)?)").matcher(content);
            while (matcher.find()) {
                UUID uuid = UUID.fromString(matcher.group(1));
                double value = Double.parseDouble(matcher.group(2));
                points.put(uuid, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File file = new File(plugin.getDataFolder(), "points.json");
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<UUID, Double> entry : points.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(entry.getKey()).append("\":").append(entry.getValue());
        }
        sb.append('}');
        try {
            Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}