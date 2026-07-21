package com.tahai.carryon;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class CarryManager {

    private final Map<Player, CarriedData> carriedMap = new HashMap<>();
    private final Set<String> allowedEntities = new HashSet<>();
    private final Set<String> allowedBlocks = new HashSet<>();

    public CarryManager(Plugin plugin) {
        plugin.saveDefaultConfig();
        loadWhitelist(plugin);
    }

    private void loadWhitelist(Plugin plugin) {
        List<String> entities = plugin.getConfig().getStringList("allowed-entities");
        for (String s : entities) {
            allowedEntities.add(s.toLowerCase());
        }
        List<String> blocks = plugin.getConfig().getStringList("allowed-blocks");
        for (String s : blocks) {
            allowedBlocks.add(s.toLowerCase());
        }
    }

    public CarriedData getCarried(Player player) {
        return carriedMap.get(player);
    }

    public void setCarried(Player player, CarriedData data) {
        carriedMap.put(player, data);
    }

    public void removeCarried(Player player) {
        carriedMap.remove(player);
    }

    public boolean isEntityAllowed(EntityType type) {
        String key = type.getKey().toString().toLowerCase(); // 例如：minecraft:zombie
        return allowedEntities.contains(key);
    }

    public boolean isBlockAllowed(Material material) {
        String key = material.getKey().toString().toLowerCase(); // 例如：minecraft:stone
        return allowedBlocks.contains(key);
    }

    public void save() {
        // No persistent data to save for CarryManager itself.
    }

    public void shutdown() {
        carriedMap.clear();
    }
}