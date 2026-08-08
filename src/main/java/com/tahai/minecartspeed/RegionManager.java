package com.tahai.minecartspeed;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionManager {
    private final File file;
    private final List<Region> regions = new ArrayList<>();

    public RegionManager(File dataFolder) {
        file = new File(dataFolder, "regions.yml");
        load();
    }

    public void addRegion(World world, Location loc1, Location loc2, double speed) {
        Region region = new Region(world, loc1, loc2, speed);
        regions.removeIf(existing -> existing.intersects(region));
        regions.add(region);
        save();
    }

    public double getSpeedAt(World world, Location location) {
        for (Region region : regions) {
            if (region.worldName.equals(world.getName()) && region.contains(location)) {
                return region.speed;
            }
        }
        return -1.0;
    }

    public void save() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Region region : regions) {
            list.add(region.serialize());
        }
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("regions", list);
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> map : cfg.getMapList("regions")) {
            regions.add(Region.deserialize(map));
        }
    }

    private static class Region {
        final String worldName;
        final double minX, minY, minZ, maxX, maxY, maxZ;
        final double speed;

        Region(World world, Location loc1, Location loc2, double speed) {
            this.worldName = world.getName();
            this.minX = Math.min(loc1.getX(), loc2.getX());
            this.minY = Math.min(loc1.getY(), loc2.getY());
            this.minZ = Math.min(loc1.getZ(), loc2.getZ());
            this.maxX = Math.max(loc1.getX(), loc2.getX());
            this.maxY = Math.max(loc1.getY(), loc2.getY());
            this.maxZ = Math.max(loc1.getZ(), loc2.getZ());
            this.speed = speed;
        }

        Region(String worldName, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double speed) {
            this.worldName = worldName;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.speed = speed;
        }

        boolean contains(Location loc) {
            if (!worldName.equals(loc.getWorld().getName())) return false;
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }

        boolean intersects(Region other) {
            return worldName.equals(other.worldName)
                    && minX <= other.maxX && maxX >= other.minX
                    && minY <= other.maxY && maxY >= other.minY
                    && minZ <= other.maxZ && maxZ >= other.minZ;
        }

        Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("world", worldName);
            map.put("minX", minX);
            map.put("minY", minY);
            map.put("minZ", minZ);
            map.put("maxX", maxX);
            map.put("maxY", maxY);
            map.put("maxZ", maxZ);
            map.put("speed", speed);
            return map;
        }

        static Region deserialize(Map<?, ?> map) {
            String world = (String) map.get("world");
            double minX = ((Number) map.get("minX")).doubleValue();
            double minY = ((Number) map.get("minY")).doubleValue();
            double minZ = ((Number) map.get("minZ")).doubleValue();
            double maxX = ((Number) map.get("maxX")).doubleValue();
            double maxY = ((Number) map.get("maxY")).doubleValue();
            double maxZ = ((Number) map.get("maxZ")).doubleValue();
            double speed = ((Number) map.get("speed")).doubleValue();
            return new Region(world, minX, minY, minZ, maxX, maxY, maxZ, speed);
        }
    }
}