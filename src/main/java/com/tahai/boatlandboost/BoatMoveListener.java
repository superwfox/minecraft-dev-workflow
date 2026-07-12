package com.tahai.boatlandboost;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class BoatMoveListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Boat)) return;
        Boat boat = (Boat) event.getVehicle();
        if (boat.getPassengers().stream().noneMatch(entity -> entity instanceof Player)) return;

        Location loc = boat.getLocation();
        Material below = loc.getBlock().getRelative(0, -1, 0).getType();
        boolean onWater = (below == Material.WATER || below == Material.BUBBLE_COLUMN);

        if (!onWater) {
            Vector velocity = boat.getVelocity();
            Vector forward = loc.getDirection().setY(0).normalize();
            boat.setVelocity(velocity.add(forward.multiply(0.2)));
        }

        Location to = event.getTo();
        Vector dir = to.toVector().subtract(event.getFrom().toVector()).setY(0).normalize();
        if (dir.lengthSquared() == 0) return;

        Location front = to.clone().add(dir.multiply(1.0));
        front.setY(to.getBlockY());
        Block frontBlock = front.getBlock();
        if (frontBlock.getType().isSolid()) {
            Location newTo = to.clone();
            newTo.setY(frontBlock.getY() + 1.0);
            event.setTo(newTo);
        }
    }
}