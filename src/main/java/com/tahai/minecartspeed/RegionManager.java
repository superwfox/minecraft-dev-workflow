package com.tahai.minecartspeed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class RegionManager {

    private final Map<String, List<RegionData>> regions = new HashMap<>();

    public RegionManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MinecartSpeed");
        if (plugin == null) {
            throw new IllegalStateException("MinecartSpeed plugin not found");
        }
        File file = new File(plugin.getDataFolder(), "regions.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("regions");
        if (section == null) {
            return;
        }
        for (String world : section.getKeys(false)) {
            List<?> rawList = section.getList(world);
            if (rawList == null) {
                continue;
            }
            List<RegionData> list = new ArrayList<>();
            for (Object obj : rawList) {
                if (!(obj instanceof Map<?, ?> map)) {
                    continue;
                }
                double x1 = ((Number) map.get("x1")).doubleValue();
                double y1 = ((Number) map.get("y1")).doubleValue();
                double z1 = ((Number) map.get("z1")).doubleValue();
                double x2 = ((Number) map.get("x2")).doubleValue();
                double y2 = ((Number) map.get("y2")).doubleValue();
                double z2 = ((Number) map.get("z2")).doubleValue();
                double speed = ((Number) map.get("speed")).doubleValue();
                list.add(new RegionData(world, x1, y1, z1, x2, y2, z2, speed));
            }
            regions.put(world, list);
        }
    }

    public void save() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MinecartSpeed");
        if (plugin == null) {
            return;
        }
        File file = new File(plugin.getDataFolder(), "regions.yml");
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection section = cfg.createSection("regions");
        for (Map.Entry<String, List<RegionData>> entry : regions.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (RegionData r : entry.getValue()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("x1", r.getX1());
                map.put("y1", r.getY1());
                map.put("z1", r.getZ1());
                map.put("x2", r.getX2());
                map.put("y2", r.getY2());
                map.put("z2", r.getZ2());
                map.put("speed", r.getSpeed());
                list.add(map);
            }
            section.set(entry.getKey(), list);
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save regions.yml: " + e.getMessage());
        }
    }

    public void addRegion(RegionData region) {
        List<RegionData> list = regions.computeIfAbsent(region.getWorld(), k -> new ArrayList<>());
        list.removeIf(existing -> overlaps(existing, region));
        list.add(region);
    }

    public boolean removeRegion(RegionData region) {
        List<RegionData> list = regions.get(region.getWorld());
        if (list == null) {
            return false;
        }
        boolean removed = list.removeIf(existing -> sameRegion(existing, region));
        if (list.isEmpty()) {
            regions.remove(region.getWorld());
        }
        return removed;
    }

    public List<RegionData> getRegions(String world) {
        List<RegionData> list = regions.get(world);
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
    }

    private boolean overlaps(RegionData a, RegionData b) {
        return a.getWorld().equals(b.getWorld())
                && Math.min(a.getX1(), a.getX2()) <= Math.max(b.getX1(), b.getX2())
                && Math.min(b.getX1(), b.getX2()) <= Math.max(a.getX1(), a.getX2())
                && Math.min(a.getY1(), a.getY2()) <= Math.max(b.getY1(), b.getY2())
                && Math.min(b.getY1(), b.getY2()) <= Math.max(a.getY1(), a.getY2())
                && Math.min(a.getZ1(), a.getZ2()) <= Math.max(b.getZ1(), b.getZ2())
                && Math.min(b.getZ1(), b.getZ2()) <= Math.max(a.getZ1(), a.getZ2());
    }

    private boolean sameRegion(RegionData a, RegionData b) {
        return a.getWorld().equals(b.getWorld())
                && a.getX1() == b.getX1() && a.getY1() == b.getY1() && a.getZ1() == b.getZ1()
                && a.getX2() == b.getX2() && a.getY2() == b.getY2() && a.getZ2() == b.getZ2();
    }
}