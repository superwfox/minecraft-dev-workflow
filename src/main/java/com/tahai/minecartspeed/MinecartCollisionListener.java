package com.tahai.minecartspeed;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.World;
import org.bukkit.ChatColor;

public class MinecartCollisionListener implements Listener {

    private final RegionManager regionManager;

    public MinecartCollisionListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!(vehicle instanceof Minecart)) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        Location location = vehicle.getLocation();
        World world = location.getWorld();

        double regionSpeed = regionManager.getSpeedAt(world, location);

        if (regionSpeed > 0) {
            vehicle.setVelocity(new Vector(0, 0, 0));
            player.sendActionBar(ChatColor.YELLOW + "急停生效！");
        } else {
            player.sendActionBar(ChatColor.GRAY + "未检测到有效速度区域");
        }
    }
}