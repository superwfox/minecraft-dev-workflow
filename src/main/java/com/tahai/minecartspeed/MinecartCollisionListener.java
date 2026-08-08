package com.tahai.minecartspeed;

import org.bukkit.Location;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;

public class MinecartCollisionListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!(vehicle instanceof Minecart)) return;

        Location loc = vehicle.getLocation();
        if (!(loc.getBlock().getBlockData() instanceof Rail)) return;

        vehicle.setVelocity(new Vector(0, 0, 0));
    }
}