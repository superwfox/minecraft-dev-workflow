package com.tahai.unpc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NpcLookTask extends BukkitRunnable {

    private final NpcManager npcManager;

    public NpcLookTask(NpcManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public void run() {
        for (NpcData data : loadNpcData()) {
            World world = Bukkit.getWorld(data.getWorldName());
            if (world == null) continue;

            ArmorStand stand = findArmorStand(new Location(world, data.getX(), data.getY(), data.getZ()));
            if (stand == null) continue;

            Player nearest = findNearestPlayer(stand);
            if (nearest == null) continue;

            LivingEntity livingEntity = (LivingEntity) stand;
            Vector direction = nearest.getEyeLocation().toVector()
                    .subtract(livingEntity.getEyeLocation().toVector());
            if (direction.lengthSquared() == 0.0) continue;

            Location look = stand.getLocation().clone();
            look.setDirection(direction);
            stand.setRotation(look.getYaw(), look.getPitch());
        }
    }

    private List<NpcData> loadNpcData() {
        List<NpcData> result = new ArrayList<>();

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Unpc");
        if (plugin == null) return result;

        List<?> npcList = plugin.getConfig().getList("npcs");
        if (npcList == null) return result;

        for (Object object : npcList) {
            if (!(object instanceof Map<?, ?> map)) continue;

            int id = asInt(map.get("id"));
            String worldName = map.get("worldName") == null ? "" : map.get("worldName").toString();
            double x = asDouble(map.get("x"));
            double y = asDouble(map.get("y"));
            double z = asDouble(map.get("z"));
            float yaw = (float) asDouble(map.get("yaw"));
            float pitch = (float) asDouble(map.get("pitch"));
            String displayText = map.get("displayText") == null ? "" : map.get("displayText").toString();
            String skinPlayerName = map.get("skinPlayerName") == null ? "" : map.get("skinPlayerName").toString();
            String skinTexture = map.get("skinTexture") == null ? "" : map.get("skinTexture").toString();
            String skinSignature = map.get("skinSignature") == null ? "" : map.get("skinSignature").toString();
            String command = map.get("command") == null ? "" : map.get("command").toString();
            String commandExecutor = map.get("commandExecutor") == null ? "player" : map.get("commandExecutor").toString();

            result.add(new NpcData(id, worldName, x, y, z, yaw, pitch, displayText,
                    skinPlayerName, skinTexture, skinSignature, command, commandExecutor));
        }

        return result;
    }

    private ArmorStand findArmorStand(Location location) {
        ArmorStand nearest = null;
        double closest = Double.MAX_VALUE;

        for (Entity entity : location.getWorld().getNearbyEntities(location, 1.0, 2.0, 1.0)) {
            if (entity instanceof ArmorStand stand) {
                double distanceSquared = stand.getLocation().distanceSquared(location);
                if (distanceSquared < closest) {
                    closest = distanceSquared;
                    nearest = stand;
                }
            }
        }

        return nearest;
    }

    private Player findNearestPlayer(ArmorStand stand) {
        Player nearest = null;
        double closest = 25.0;

        for (Entity entity : stand.getNearbyEntities(5.0, 5.0, 5.0)) {
            if (entity instanceof Player player) {
                double distanceSquared = player.getLocation().distanceSquared(stand.getLocation());
                if (distanceSquared <= closest) {
                    closest = distanceSquared;
                    nearest = player;
                }
            }
        }

        return nearest;
    }

    private int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private double asDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }
}