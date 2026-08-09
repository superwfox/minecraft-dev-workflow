package com.tahai.sect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SectClan {
    private String name;
    private UUID leaderUuid;
    private Map<UUID, SectRank> members;
    private int level;
    private int killCount;
    private String world;
    private String regionName;

    public SectClan() {
        this.name = "";
        this.leaderUuid = null;
        this.members = new LinkedHashMap<>();
        this.level = 1;
        this.killCount = 0;
        this.world = "";
        this.regionName = "";
    }

    public SectClan(String name, UUID leaderUuid, Map<UUID, SectRank> members,
                    int level, int killCount, String world, String regionName) {
        this.name = name;
        this.leaderUuid = leaderUuid;
        this.members = members;
        this.level = level;
        this.killCount = killCount;
        this.world = world;
        this.regionName = regionName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getLeaderUuid() {
        return leaderUuid;
    }

    public void setLeaderUuid(UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public Map<UUID, SectRank> getMembers() {
        return members;
    }

    public void setMembers(Map<UUID, SectRank> members) {
        this.members = members;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getKillCount() {
        return killCount;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    @Override
    public String toString() {
        return "SectClan{name='" + name + "', leaderUuid=" + leaderUuid +
                ", members=" + members + ", level=" + level +
                ", killCount=" + killCount + ", world='" + world +
                "', regionName='" + regionName + "'}";
    }
}