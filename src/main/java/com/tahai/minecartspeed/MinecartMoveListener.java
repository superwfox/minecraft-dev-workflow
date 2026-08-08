package com.tahai.minecartspeed;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.Plugin;

public class MinecartMoveListener implements Listener {

    private static final double DEFAULT_SPEED = 0.4D;
    private static final double DECREMENT = 0.05D;

    private final RegionManager regionManager;
    private final Set<UUID> boosted = new HashSet<>();
    private final String actionbarTemplate;

    public MinecartMoveListener(RegionManager regionManager) {
        this.regionManager = regionManager;
        String fallback = ChatColor.YELLOW + "当前限速: " + ChatColor.BOLD + "{speed}" + ChatColor.YELLOW;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MinecartSpeed");
        if (plugin == null) {
            this.actionbarTemplate = fallback;
        } else {
            String template = plugin.getConfig().getString("messages.actionbar",
                    plugin.getConfig().getString("messages.enter", fallback));
            this.actionbarTemplate = ChatColor.translateAlternateColorCodes('&', template == null ? fallback : template);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }

        Minecart cart = (Minecart) event.getVehicle();
        Location to = event.getTo();
        double speed = regionManager.getSpeedAt(to.getWorld(), to);
        UUID id = cart.getUniqueId();

        if (speed > 0.0D) {
            cart.setMaxSpeed(speed);
            boosted.add(id);
            sendActionBar(cart, speed);
            return;
        }

        if (boosted.remove(id)) {
            double next = Math.max(DEFAULT_SPEED, cart.getMaxSpeed() - DECREMENT);
            cart.setMaxSpeed(next);
            if (next > DEFAULT_SPEED) {
                boosted.add(id);
            }
        }
    }

    private void sendActionBar(Minecart cart, double speed) {
        String message = actionbarTemplate.replace("{speed}", String.format(Locale.ROOT, "%.2f", speed));
        for (Entity entity : cart.getPassengers()) {
            if (entity instanceof Player) {
                ((Player) entity).sendActionBar(message);
            }
        }
    }
}