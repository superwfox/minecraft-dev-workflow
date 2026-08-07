package com.tahai.scanmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class DataManager {

    private final Plugin plugin;
    private final File dataFile;
    private final LinkedHashMap<String, LinkedHashSet<UUID>> ipToUUIDs = new LinkedHashMap<>();
    private final LinkedHashMap<UUID, LinkedHashSet<String>> uuidToIPs = new LinkedHashMap<>();

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    public void addMapping(UUID uuid, String ip) {
        if (uuid == null || ip == null) {
            return;
        }
        ipToUUIDs.computeIfAbsent(ip, k -> new LinkedHashSet<>()).remove(uuid);
        ipToUUIDs.get(ip).add(uuid);
        uuidToIPs.computeIfAbsent(uuid, k -> new LinkedHashSet<>()).remove(ip);
        uuidToIPs.get(uuid).add(ip);
        save();
    }

    public List<UUID> getUUIDsByIP(String ip) {
        LinkedHashSet<UUID> uuids = ipToUUIDs.get(ip);
        return uuids == null ? new ArrayList<>() : new ArrayList<>(uuids);
    }

    public List<String> getIPsByUUID(UUID uuid) {
        LinkedHashSet<String> ips = uuidToIPs.get(uuid);
        return ips == null ? new ArrayList<>() : new ArrayList<>(ips);
    }

    public void load() {
        ipToUUIDs.clear();
        uuidToIPs.clear();
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection ipSection = cfg.getConfigurationSection("ipToUUIDs");
        if (ipSection != null) {
            for (String ip : ipSection.getKeys(false)) {
                LinkedHashSet<UUID> uuids = new LinkedHashSet<>();
                for (String uuidStr : ipSection.getStringList(ip)) {
                    try {
                        uuids.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (!uuids.isEmpty()) {
                    ipToUUIDs.put(ip, uuids);
                }
            }
        }
        ConfigurationSection uuidSection = cfg.getConfigurationSection("uuidToIPs");
        if (uuidSection != null) {
            for (String uuidStr : uuidSection.getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                LinkedHashSet<String> ips = new LinkedHashSet<>(uuidSection.getStringList(uuidStr));
                if (!ips.isEmpty()) {
                    uuidToIPs.put(uuid, ips);
                }
            }
        }
    }

    public void save() {
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<String, LinkedHashSet<UUID>> entry : ipToUUIDs.entrySet()) {
            List<String> uuidStrs = new ArrayList<>();
            for (UUID uuid : entry.getValue()) {
                uuidStrs.add(uuid.toString());
            }
            cfg.set("ipToUUIDs." + entry.getKey(), uuidStrs);
        }
        for (Map.Entry<UUID, LinkedHashSet<String>> entry : uuidToIPs.entrySet()) {
            cfg.set("uuidToIPs." + entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save data.yml: " + e.getMessage());
        }
    }
}