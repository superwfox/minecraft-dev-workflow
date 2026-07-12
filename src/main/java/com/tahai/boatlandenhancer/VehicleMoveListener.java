package com.tahai.boatlandenhancer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class VehicleMoveListener implements Listener {

    private final SpeedConfigManager configManager;

    public VehicleMoveListener(SpeedConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Boat boat)) return;

        Location to = event.getTo();
        Block below = to.clone().subtract(0, 1, 0).getBlock();
        if (below.getType().isAir()) return;

        double multiplier = 1.0;
        for (Entity passenger : boat.getPassengers()) {
            if (passenger instanceof Player player) {
                PlayerInventory inv = player.getInventory();
                ItemStack boots = inv.getBoots();
                if (boots != null && boots.getType() != Material.AIR) {
                    multiplier *= getMultiplier(boots, getEnchantment("soul_speed"));
                    multiplier *= getMultiplier(boots, getEnchantment("depth_strider"));
                    multiplier *= getMultiplier(boots, getEnchantment("frost_walker"));
                }
            }
        }
        if (multiplier != 1.0) {
            Vector velocity = boat.getVelocity();
            boat.setVelocity(velocity.multiply(multiplier));
        }

        float yaw = boat.getLocation().getYaw();
        Vector direction = new Vector(Math.cos(Math.toRadians(yaw)), 0, Math.sin(Math.toRadians(yaw))).normalize();
        Location frontLoc = to.clone().add(direction.getX(), 0, direction.getZ());
        Location aboveFrontLoc = frontLoc.clone().add(0, 1, 0);

        Block frontBlock = frontLoc.getBlock();
        Block aboveFrontBlock = aboveFrontLoc.getBlock();

        if (!frontBlock.getType().isAir() && frontBlock.getType().isSolid() &&
                aboveFrontBlock.getType().isAir()) {
            Location newLoc = to.clone();
            newLoc.setY(frontBlock.getY() + 1);
            event.setTo(newLoc);
        }
    }

    private double getMultiplier(ItemStack item, Enchantment enchant) {
        int level = item.getEnchantmentLevel(enchant);
        if (level <= 0) return 1.0;
        return configManager.getSpeedMultiplier(enchant, level);
    }

    private Enchantment getEnchantment(String name) {
        return Bukkit.getRegistry(Enchantment.class).get(NamespacedKey.minecraft(name));
    }
}