package com.tahai.qinglong.model;

import java.util.List;

public class MapData {
    private String key;
    private String displayName;
    private List<String> lore;
    private String world;
    private SpawnPoint spawnPoint;
    private String crossServerCommand;

    public MapData() {}

    public MapData(String key, String displayName, List<String> lore, String world, SpawnPoint spawnPoint, String crossServerCommand) {
        this.key = key;
        this.displayName = displayName;
        this.lore = lore;
        this.world = world;
        this.spawnPoint = spawnPoint;
        this.crossServerCommand = crossServerCommand;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public SpawnPoint getSpawnPoint() { return spawnPoint; }
    public void setSpawnPoint(SpawnPoint spawnPoint) { this.spawnPoint = spawnPoint; }
    public String getCrossServerCommand() { return crossServerCommand; }
    public void setCrossServerCommand(String crossServerCommand) { this.crossServerCommand = crossServerCommand; }

    public static class SpawnPoint {
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;

        public SpawnPoint() {}

        public SpawnPoint(double x, double y, double z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        public double getZ() { return z; }
        public void setZ(double z) { this.z = z; }
        public float getYaw() { return yaw; }
        public void setYaw(float yaw) { this.yaw = yaw; }
        public float getPitch() { return pitch; }
        public void setPitch(float pitch) { this.pitch = pitch; }
    }
}