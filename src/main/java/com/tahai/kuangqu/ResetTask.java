package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ResetTask extends BukkitRunnable {

    private final DataManager dataManager;

    public ResetTask(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        ConfigurationSection zones = dataManager.getAllZones();
        if (zones == null) return;

        for (String zoneName : zones.getKeys(false)) {
            ConfigurationSection zone = zones.getConfigurationSection(zoneName);
            if (zone == null) continue;

            long interval = zone.getLong("reset-interval", 3600);
            long lastReset = zone.getLong("last-reset-time", 0);
            long now = System.currentTimeMillis();

            if (lastReset == 0) {
                zone.set("last-reset-time", now);
                dataManager.saveConfig();
                continue;
            }

            if (now - lastReset < interval * 1000L) continue;

            String worldName = zone.getString("world");
            if (worldName == null) continue;
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            int x1 = zone.getInt("x1");
            int y1 = zone.getInt("y1");
            int z1 = zone.getInt("z1");
            int x2 = zone.getInt("x2");
            int y2 = zone.getInt("y2");
            int z2 = zone.getInt("z2");

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            String fillName = zone.getString("fill-material", "STONE");
            Material fillMat = Material.getMaterial(fillName.toUpperCase());
            if (fillMat == null) fillMat = Material.STONE;

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        world.getBlockAt(x, y, z).setType(fillMat);
                    }
                }
            }

            List<Map<?, ?>> oreList = zone.getMapList("ore-distribution");
            if (oreList != null && !oreList.isEmpty()) {
                List<OreEntry> ores = new ArrayList<>();
                for (Map<?, ?> entry : oreList) {
                    String matName = (String) entry.get("material");
                    double chance = ((Number) entry.getOrDefault("chance", 0.0)).doubleValue();
                    Material mat = Material.getMaterial(matName.toUpperCase());
                    if (mat != null) {
                        ores.add(new OreEntry(mat, chance));
                    }
                }
                if (!ores.isEmpty()) {
                    Random rand = new Random();
                    for (int x = minX; x <= maxX; x++) {
                        for (int y = minY; y <= maxY; y++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                double r = rand.nextDouble();
                                for (OreEntry ore : ores) {
                                    if (r < ore.chance) {
                                        world.getBlockAt(x, y, z).setType(ore.material);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            zone.set("last-reset-time", now);
            dataManager.saveConfig();

            String msg = dataManager.getConfig().getString("messages.reset", "&e矿区 {zone} 已重置!");
            msg = ChatColor.translateAlternateColorCodes('&', msg.replace("{zone}", zoneName));
            for (Player p : world.getPlayers()) {
                Location loc = p.getLocation();
                if (loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                    loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                    loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ) {
                    p.sendMessage(msg);
                }
            }
        }
    }

    private static class OreEntry {
        final Material material;
        final double chance;
        OreEntry(Material material, double chance) {
            this.material = material;
            this.chance = chance;
        }
    }
}