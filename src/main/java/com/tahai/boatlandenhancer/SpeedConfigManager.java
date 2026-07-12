package com.tahai.boatlandenhancer;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SpeedConfigManager {

    private final Map<Enchantment, Map<Integer, Double>> speedMap;

    public SpeedConfigManager(Plugin plugin) {
        this.speedMap = new HashMap<>();
        load(plugin);
    }

    private void load(Plugin plugin) {
        InputStream in = plugin.getResource("speed_config.txt");
        if (in == null) {
            plugin.getLogger().warning("speed_config.txt not found in plugin jar.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(":");
                if (parts.length != 3) {
                    plugin.getLogger().warning("Invalid line in speed_config.txt: " + line);
                    continue;
                }

                String enchantName = parts[0].trim().toLowerCase();
                int level;
                double speed;
                try {
                    level = Integer.parseInt(parts[1].trim());
                    speed = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid number format in speed_config.txt: " + line);
                    continue;
                }

                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
                if (enchantment == null) {
                    plugin.getLogger().warning("Unknown enchantment '" + enchantName + "' in speed_config.txt");
                    continue;
                }

                speedMap.computeIfAbsent(enchantment, k -> new HashMap<>()).put(level, speed);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read speed_config.txt", e);
        }
    }

    public double getSpeedMultiplier(Enchantment enchantment, int level) {
        Map<Integer, Double> levelMap = speedMap.get(enchantment);
        if (levelMap == null) {
            return 1.0;
        }
        return levelMap.getOrDefault(level, 1.0);
    }
}