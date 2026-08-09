package com.tahai.sect;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final Plugin plugin;
    private final File dataFile;
    private final Map<String, SectClan> clans = new HashMap<>();

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    public void load() {
        clans.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection clansSection = cfg.getConfigurationSection("clans");
        if (clansSection == null) return;

        for (String name : clansSection.getKeys(false)) {
            ConfigurationSection cs = clansSection.getConfigurationSection(name);
            if (cs == null) continue;

            UUID leaderUuid = UUID.fromString(cs.getString("leader"));
            int level = cs.getInt("level", 1);
            int killCount = cs.getInt("killCount", 0);
            String world = cs.getString("world", "world");
            String regionName = cs.getString("regionName");

            Map<UUID, SectRank> members = new HashMap<>();
            ConfigurationSection ms = cs.getConfigurationSection("members");
            if (ms != null) {
                for (String uuidStr : ms.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        SectRank rank = SectRank.valueOf(ms.getString(uuidStr));
                        members.put(uuid, rank);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            clans.put(name, new SectClan(name, leaderUuid, members, level, killCount, world, regionName));
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection clansSection = cfg.createSection("clans");

        for (SectClan clan : clans.values()) {
            ConfigurationSection cs = clansSection.createSection(clan.getName());
            cs.set("leader", clan.getLeaderUuid().toString());
            cs.set("level", clan.getLevel());
            cs.set("killCount", clan.getKillCount());
            cs.set("world", clan.getWorld());
            cs.set("regionName", clan.getRegionName());

            ConfigurationSection ms = cs.createSection("members");
            for (Map.Entry<UUID, SectRank> entry : clan.getMembers().entrySet()) {
                ms.set(entry.getKey().toString(), entry.getValue().name());
            }
        }

        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save data.yml: " + e.getMessage());
        }
    }

    public SectClan createSect(String name, UUID leaderUuid, String world) {
        if (clans.containsKey(name)) return null;
        SectClan clan = new SectClan(name, leaderUuid, new HashMap<>(), 1, 0, world, null);
        clans.put(name, clan);
        save();
        return clan;
    }

    public boolean deleteSect(String name) {
        if (clans.remove(name) == null) return false;
        save();
        return true;
    }

    public SectClan getSect(String name) {
        return clans.get(name);
    }

    public boolean setRank(String name, UUID playerUuid, SectRank rank) {
        SectClan clan = getSect(name);
        if (clan == null || rank == null) return false;
        clan.getMembers().put(playerUuid, rank);
        save();
        return true;
    }

    public boolean addKillCount(String name, int amount) {
        SectClan clan = getSect(name);
        if (clan == null) return false;
        clan.setKillCount(clan.getKillCount() + amount);
        save();
        return true;
    }
}