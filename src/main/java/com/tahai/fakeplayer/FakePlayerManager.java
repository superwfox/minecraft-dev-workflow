package com.tahai.fakeplayer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FakePlayerManager {

    private final Map<String, Entity> fakePlayers = new LinkedHashMap<>();

    public synchronized Entity spawnFakePlayer(String name, Location location) {
        if (name == null || location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Name and location must not be null, world must exist");
        }
        if (fakePlayers.containsKey(name)) {
            return null;
        }
        Entity entity = createFakePlayer(location, name);
        Chunk chunk = entity.getLocation().getChunk();
        location.getWorld().addPluginChunkTicket(chunk.getX(), chunk.getZ(), getPlugin());
        fakePlayers.put(name, entity);
        return entity;
    }

    public synchronized boolean removeFakePlayer(String name) {
        Entity entity = fakePlayers.remove(name);
        if (entity == null) {
            return false;
        }
        Chunk chunk = entity.getLocation().getChunk();
        entity.getWorld().removePluginChunkTicket(chunk.getX(), chunk.getZ(), getPlugin());
        entity.remove();
        return true;
    }

    public synchronized void clearChunkTickets() {
        for (Entity entity : fakePlayers.values()) {
            Chunk chunk = entity.getLocation().getChunk();
            entity.getWorld().removePluginChunkTicket(chunk.getX(), chunk.getZ(), getPlugin());
        }
    }

    public synchronized Entity getFakePlayer(String name) {
        return fakePlayers.get(name);
    }

    public synchronized Map<String, Entity> getFakePlayers() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(fakePlayers));
    }

    private Plugin getPlugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("FakePlayer");
        if (plugin == null) {
            throw new IllegalStateException("FakePlayer plugin is not loaded");
        }
        return plugin;
    }

    private Entity createFakePlayer(Location location, String name) {
        Entity entity = location.getWorld().spawnEntity(location, EntityType.PLAYER);
        if (entity == null) {
            throw new IllegalStateException("Failed to create fake player entity for " + name);
        }
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        return entity;
    }
}