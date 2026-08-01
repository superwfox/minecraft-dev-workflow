package com.tahai.sect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Sect {
    private final String name;
    private UUID owner;
    private int level;
    private final Map<UUID, String> members;
    private final Set<UUID> invites;

    public Sect(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.level = 1;
        this.members = new HashMap<>();
        this.members.put(owner, "owner");
        this.invites = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<UUID, String> getMembers() {
        return members;
    }

    public Set<UUID> getInvites() {
        return invites;
    }

    public int getMemberCount() {
        return members.size();
    }

    public boolean isMember(UUID player) {
        return members.containsKey(player);
    }

    public boolean isInvited(UUID player) {
        return invites.contains(player);
    }

    public String getRole(UUID player) {
        return members.getOrDefault(player, "");
    }

    public boolean setRole(UUID player, String role) {
        if (!members.containsKey(player)) {
            return false;
        }
        members.put(player, role);
        if ("owner".equals(role)) {
            this.owner = player;
        }
        return true;
    }

    public void addMember(UUID player) {
        members.put(player, "member");
    }

    public void removeMember(UUID player) {
        members.remove(player);
    }

    public void addInvite(UUID player) {
        invites.add(player);
    }

    public void removeInvite(UUID player) {
        invites.remove(player);
    }
}