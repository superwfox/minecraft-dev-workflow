package com.tahai.minecartspeed;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MinecartMoveListener implements Listener {
    private final RegionManager regionManager = new RegionManager();
    private final Map<UUID, Boolean> inRegion = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle().getType() != EntityType.MINECART) return;
        Minecart minecart = (Minecart) event.getVehicle();
        Location loc = minecart.getLocation();

        List<RegionData> regions = regionManager.getRegions(loc.getWorld().getName());
        double minSpeed = 0;
        boolean inAny = false;
        for (RegionData region : regions) {
            if (isInside(loc, region)) {
                double speed = region.getSpeed();
                if (!inAny || speed < minSpeed) {
                    minSpeed = speed;
                    inAny = true;
                }
            }
        }

        UUID uuid = minecart.getUniqueId();
        boolean wasIn = inRegion.getOrDefault(uuid, false);

        if (inAny) {
            inRegion.put(uuid, true);
            Vector vel = minecart.getVelocity();
            if (vel.lengthSquared() > 1.0E-6) {
                Vector newVel = vel.clone().normalize().multiply(minSpeed);
                minecart.setVelocity(newVel);
            }
            ConfigManager cm = ConfigManager.getInstance();
            String msg = wasIn ? cm.getActionbarChange() : cm.getActionbarEnter();
            sendActionBar(minecart, msg);
        } else {
            if (wasIn) {
                inRegion.remove(uuid);
                sendActionBar(minecart, ConfigManager.getInstance().getActionbarLeave());
            }
        }
    }

    private boolean isInside(Location loc, RegionData r) {
        if (!loc.getWorld().getName().equals(r.getWorld())) return false;
        double x1 = Math.min(r.getX1(), r.getX2());
        double x2 = Math.max(r.getX1(), r.getX2());
        double y1 = Math.min(r.getY1(), r.getY2());
        double y2 = Math.max(r.getY1(), r.getY2());
        double z1 = Math.min(r.getZ1(), r.getZ2());
        double z2 = Math.max(r.getZ1(), r.getZ2());
        if (ConfigManager.getInstance().isIgnoreY()) {
            return loc.getX() >= x1 && loc.getX() <= x2 && loc.getZ() >= z1 && loc.getZ() <= z2;
        }
        return loc.getX() >= x1 && loc.getX() <= x2 && loc.getY() >= y1 && loc.getY() <= y2 && loc.getZ() >= z1 && loc.getZ() <= z2;
    }

    private void sendActionBar(Minecart minecart, String msg) {
        if (msg == null || msg.isEmpty()) return;
        for (Entity passenger : minecart.getPassengers()) {
            if (passenger instanceof Player) {
                ((Player) passenger).sendActionBar(msg);
            }
        }
    }
}