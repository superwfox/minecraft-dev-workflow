package com.tahai.unpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class NpcManager {

    private final Map<String, NpcData> npcs = new HashMap<>();
    private final File dataFile;

    public NpcManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Unpc");
        if (plugin == null) {
            throw new IllegalStateException("Unpc plugin not found");
        }
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.dataFile = new File(dataFolder, "npcs.bin");
        plugin.setMetadata("UnpcNpcManager", new FixedMetadataValue(plugin, this));
        load();
    }

    public static NpcManager fromPlugin(Plugin plugin) {
        for (MetadataValue value : plugin.getMetadata("UnpcNpcManager")) {
            if (value.getOwningPlugin().equals(plugin)) {
                return (NpcManager) value.value();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!dataFile.exists()) {
            save();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Object loaded = ois.readObject();
            if (loaded instanceof Map) {
                npcs.clear();
                npcs.putAll((Map<String, NpcData>) loaded);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(npcs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        save();
    }

    public NpcData createNpc(String name, String skin, List<String> commands, Location loc) {
        if (loc.getWorld() == null) return null;
        Villager entity = loc.getWorld().spawn(loc, Villager.class);
        entity.setAI(false);
        entity.setInvulnerable(true);
        entity.setGravity(false);
        entity.setSilent(true);
        entity.setRemoveWhenFarAway(false);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);

        NpcData data = new NpcData(name, skin, new ArrayList<>(commands), entity.getUniqueId(), loc);
        npcs.put(name, data);
        return data;
    }

    public NpcData getNpc(String name) {
        return npcs.get(name);
    }

    public void renameNpc(String oldName, String newName) {
        NpcData data = npcs.remove(oldName);
        if (data != null) {
            data.name = newName;
            npcs.put(newName, data);
            Entity entity = Bukkit.getEntity(data.entityId);
            if (entity != null) {
                entity.setCustomName(newName);
            }
        }
    }

    public void setSkin(String name, String skin) {
        NpcData data = npcs.get(name);
        if (data != null) {
            data.skin = skin;
        }
    }

    public void setCommands(String name, List<String> commands) {
        NpcData data = npcs.get(name);
        if (data != null) {
            data.commands = new ArrayList<>(commands);
        }
    }

    public void teleportNpc(String name, Location loc) {
        NpcData data = npcs.get(name);
        if (data == null) return;
        Entity entity = Bukkit.getEntity(data.entityId);
        if (entity != null) {
            entity.teleport(loc);
        }
        data.updateLocation(loc);
    }

    public void removeNpc(String name) {
        NpcData data = npcs.remove(name);
        if (data != null) {
            Entity entity = Bukkit.getEntity(data.entityId);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    public static class NpcData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String skin;
        private List<String> commands;
        private UUID entityId;
        private String worldName;
        private double x, y, z;
        private float yaw, pitch;

        public NpcData() {}

        public NpcData(String name, String skin, List<String> commands, UUID entityId, Location loc) {
            this.name = name;
            this.skin = skin;
            this.commands = commands;
            this.entityId = entityId;
            updateLocation(loc);
        }

        public void updateLocation(Location loc) {
            this.worldName = loc.getWorld().getName();
            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();
            this.yaw = loc.getYaw();
            this.pitch = loc.getPitch();
        }

        public Location getLocation() {
            if (worldName == null) return null;
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;
            return new Location(world, x, y, z, yaw, pitch);
        }

        public String getName() {
            return name;
        }

        public String getSkin() {
            return skin;
        }

        public List<String> getCommands() {
            return commands;
        }

        public UUID getEntityId() {
            return entityId;
        }
    }
}