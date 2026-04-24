package com.example.landplugin;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LandData {
    private final int chunkX;
    private final int chunkZ;
    private final String worldName;
    private final UUID ownerUUID;
    private final List<UUID> trustedMembers;

    public LandData(int chunkX, int chunkZ, String worldName, UUID ownerUUID) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.worldName = worldName;
        this.ownerUUID = ownerUUID;
        this.trustedMembers = new ArrayList<>();
    }

    public LandData(int chunkX, int chunkZ, String worldName, UUID ownerUUID, List<UUID> trustedMembers) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.worldName = worldName;
        this.ownerUUID = ownerUUID;
        this.trustedMembers = new ArrayList<>(trustedMembers);
    }

    public static LandData fromChunk(Chunk chunk, UUID ownerUUID) {
        return new LandData(chunk.getX(), chunk.getZ(), chunk.getWorld().getName(), ownerUUID);
    }

    public static LandData fromLocation(Location location, UUID ownerUUID) {
        return new LandData(
            location.getBlockX() >> 4,
            location.getBlockZ() >> 4,
            location.getWorld().getName(),
            ownerUUID
        );
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public String getWorldName() {
        return worldName;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public List<UUID> getTrustedMembers() {
        return new ArrayList<>(trustedMembers);
    }

    public void addTrustedMember(UUID memberUUID) {
        if (!trustedMembers.contains(memberUUID)) {
            trustedMembers.add(memberUUID);
        }
    }

    public void removeTrustedMember(UUID memberUUID) {
        trustedMembers.remove(memberUUID);
    }

    public boolean isTrusted(UUID playerUUID) {
        return ownerUUID.equals(playerUUID) || trustedMembers.contains(playerUUID);
    }

    public boolean isOwner(UUID playerUUID) {
        return ownerUUID.equals(playerUUID);
    }

    public String getChunkKey() {
        return worldName + ";" + chunkX + ";" + chunkZ;
    }

    public static String createChunkKey(int chunkX, int chunkZ, String worldName) {
        return worldName + ";" + chunkX + ";" + chunkZ;
    }
}