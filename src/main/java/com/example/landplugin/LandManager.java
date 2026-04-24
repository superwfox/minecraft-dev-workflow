package com.example.landplugin;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class LandManager {

    private final File dataFile;
    private final Map<String, LandData> landMap = new ConcurrentHashMap<>();
    private final Plugin plugin;

    public LandManager() {
        this.plugin = Bukkit.getPluginManager().getPlugin("LandPlugin");
        if (plugin == null) {
            throw new IllegalStateException("LandPlugin not found!");
        }
        this.dataFile = new File(plugin.getDataFolder(), "lands.yml");
        loadLands();
    }

    public void loadLands() {
        landMap.clear();
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = config.getConfigurationSection("lands");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection landSection = section.getConfigurationSection(key);
            if (landSection == null) continue;
            int chunkX = landSection.getInt("chunkX");
            int chunkZ = landSection.getInt("chunkZ");
            String worldName = landSection.getString("worldName");
            UUID ownerUUID = UUID.fromString(landSection.getString("ownerUUID"));
            List<UUID> trustedMembers = new ArrayList<>();
            List<String> trustedUUIDS = landSection.getStringList("trustedMembers");
            for (String uuidStr : trustedUUIDS) {
                trustedMembers.add(UUID.fromString(uuidStr));
            }
            LandData landData = new LandData(chunkX, chunkZ, worldName, ownerUUID, trustedMembers);
            landMap.put(key, landData);
        }
    }

    public void saveLands() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, LandData> entry : landMap.entrySet()) {
            LandData land = entry.getValue();
            String key = entry.getKey();
            ConfigurationSection landSection = config.createSection("lands." + key);
            landSection.set("chunkX", land.getChunkX());
            landSection.set("chunkZ", land.getChunkZ());
            landSection.set("worldName", land.getWorldName());
            landSection.set("ownerUUID", land.getOwnerUUID().toString());
            List<String> trustedUUIDS = new ArrayList<>();
            for (UUID uuid : land.getTrustedMembers()) {
                trustedUUIDS.add(uuid.toString());
            }
            landSection.set("trustedMembers", trustedUUIDS);
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save lands.yml!", e);
        }
    }

    public boolean createLand(Chunk chunk, Player player) {
        String key = LandData.createChunkKey(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
        if (landMap.containsKey(key)) {
            return false;
        }
        LandData landData = LandData.fromChunk(chunk, player.getUniqueId());
        landMap.put(key, landData);
        saveLands();
        return true;
    }

    public boolean deleteLand(Chunk chunk) {
        String key = LandData.createChunkKey(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
        LandData removed = landMap.remove(key);
        if (removed != null) {
            saveLands();
            return true;
        }
        return false;
    }

    public LandData getLand(Chunk chunk) {
        String key = LandData.createChunkKey(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
        return landMap.get(key);
    }

    public LandData getLand(Location location) {
        return getLand(location.getChunk());
    }

    public LandData getLand(int chunkX, int chunkZ, String worldName) {
        String key = LandData.createChunkKey(chunkX, chunkZ, worldName);
        return landMap.get(key);
    }

    public boolean isLandOwner(Chunk chunk, UUID playerUUID) {
        LandData land = getLand(chunk);
        return land != null && land.isOwner(playerUUID);
    }

    public boolean isLandTrusted(Chunk chunk, UUID playerUUID) {
        LandData land = getLand(chunk);
        return land != null && (land.isOwner(playerUUID) || land.isTrusted(playerUUID));
    }

    public boolean hasPermission(Chunk chunk, UUID playerUUID) {
        return isLandTrusted(chunk, playerUUID);
    }

    public boolean addTrusted(Chunk chunk, UUID targetUUID) {
        LandData land = getLand(chunk);
        if (land == null) return false;
        land.addTrustedMember(targetUUID);
        saveLands();
        return true;
    }

    public boolean removeTrusted(Chunk chunk, UUID targetUUID) {
        LandData land = getLand(chunk);
        if (land == null) return false;
        land.removeTrustedMember(targetUUID);
        saveLands();
        return true;
    }

    public Map<String, LandData> getAllLands() {
        return Collections.unmodifiableMap(landMap);
    }
}