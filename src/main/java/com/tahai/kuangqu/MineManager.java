package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class MineManager {

    private final Plugin plugin;
    private final File minesFile;
    private final Map<String, Mine> mines = new HashMap<>();

    public MineManager(Plugin plugin) {
        this.plugin = plugin;
        this.minesFile = new File(plugin.getDataFolder(), "mines.yml");
        loadMines();
    }

    public static class Mine {
        private String name;
        private String worldName;
        private int minX, minY, minZ;
        private int maxX, maxY, maxZ;
        private String resetTime; // HH:mm
        private String lastReset; // yyyy-MM-dd

        public Mine(String name, String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String resetTime) {
            this.name = name;
            this.worldName = worldName;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.resetTime = resetTime;
            this.lastReset = "";
        }

        public String getName() { return name; }
        public String getWorldName() { return worldName; }
        public int getMinX() { return minX; }
        public int getMinY() { return minY; }
        public int getMinZ() { return minZ; }
        public int getMaxX() { return maxX; }
        public int getMaxY() { return maxY; }
        public int getMaxZ() { return maxZ; }
        public String getResetTime() { return resetTime; }
        public void setResetTime(String resetTime) { this.resetTime = resetTime; }
        public String getLastReset() { return lastReset; }
        public void setLastReset(String lastReset) { this.lastReset = lastReset; }

        public void saveToConfig(ConfigurationSection section) {
            section.set("world", worldName);
            section.set("min-x", minX);
            section.set("min-y", minY);
            section.set("min-z", minZ);
            section.set("max-x", maxX);
            section.set("max-y", maxY);
            section.set("max-z", maxZ);
            section.set("reset-time", resetTime);
            section.set("last-reset", lastReset);
        }

        public static Mine fromConfig(String name, ConfigurationSection section) {
            Mine mine = new Mine(
                    name,
                    section.getString("world"),
                    section.getInt("min-x"),
                    section.getInt("min-y"),
                    section.getInt("min-z"),
                    section.getInt("max-x"),
                    section.getInt("max-y"),
                    section.getInt("max-z"),
                    section.getString("reset-time", "00:00")
            );
            mine.setLastReset(section.getString("last-reset", ""));
            return mine;
        }
    }

    private void loadMines() {
        if (!minesFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                minesFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("无法创建 mines.yml");
            }
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(minesFile);
        ConfigurationSection minesSection = cfg.getConfigurationSection("mines");
        if (minesSection == null) return;
        for (String name : minesSection.getKeys(false)) {
            ConfigurationSection mineSection = minesSection.getConfigurationSection(name);
            if (mineSection != null) {
                mines.put(name, Mine.fromConfig(name, mineSection));
            }
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection minesSection = cfg.createSection("mines");
        for (Mine mine : mines.values()) {
            ConfigurationSection mineSection = minesSection.createSection(mine.getName());
            mine.saveToConfig(mineSection);
        }
        try {
            plugin.getDataFolder().mkdirs();
            cfg.save(minesFile);
        } catch (IOException e) {
            Bukkit.getLogger().warning("保存 mines.yml 失败: " + e.getMessage());
        }
    }

    public void shutdown() {
        save();
    }

    public Mine addMine(String name, String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String resetTime) {
        if (mines.containsKey(name)) return null;
        Mine mine = new Mine(name, worldName, minX, minY, minZ, maxX, maxY, maxZ, resetTime);
        mines.put(name, mine);
        save();
        return mine;
    }

    public boolean removeMine(String name) {
        Mine removed = mines.remove(name);
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    public Mine getMineByName(String name) {
        return mines.get(name);
    }

    public List<Mine> getAllMines() {
        return new ArrayList<>(mines.values());
    }

    public Mine findMineByLocation(Location loc) {
        for (Mine mine : mines.values()) {
            if (!mine.getWorldName().equals(loc.getWorld().getName())) continue;
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            if (x >= mine.getMinX() && x <= mine.getMaxX() &&
                y >= mine.getMinY() && y <= mine.getMaxY() &&
                z >= mine.getMinZ() && z <= mine.getMaxZ()) {
                return mine;
            }
        }
        return null;
    }

    public void resetMine(Mine mine) {
        World world = Bukkit.getWorld(mine.getWorldName());
        if (world == null) return;

        Plugin kuangquPlugin = Bukkit.getPluginManager().getPlugin("Kuangqu");
        if (kuangquPlugin == null) return;
        File configFile = new File(kuangquPlugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        int diamondPercentage = config.getInt("diamond-percentage", 0);
        Random random = new Random();

        for (int x = mine.getMinX(); x <= mine.getMaxX(); x++) {
            for (int y = mine.getMinY(); y <= mine.getMaxY(); y++) {
                for (int z = mine.getMinZ(); z <= mine.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.STONE);
                    if (random.nextInt(100) < diamondPercentage) {
                        block.setType(Material.DIAMOND_ORE);
                    }
                }
            }
        }

        mine.setLastReset(LocalDate.now().toString());
        save();
    }
}