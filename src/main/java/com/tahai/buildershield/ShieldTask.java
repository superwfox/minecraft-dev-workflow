package com.tahai.buildershield;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class ShieldTask extends BukkitRunnable {

    private final ConfigManager configManager;
    private final DataManager dataManager;
    private final Map<String, Long> lastDays = new HashMap<>();

    public ShieldTask(ConfigManager configManager, DataManager dataManager) {
        this.configManager = configManager;
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BuilderShield");
        if (plugin == null) return;

        for (World world : Bukkit.getWorlds()) {
            long day = world.getFullTime() / 24000;
            Long last = lastDays.get(world.getName());
            if (last != null && day != last) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (dataManager.isShieldEnabled(p.getUniqueId())) {
                        dataManager.closeShield(p.getUniqueId());
                    }
                }
                dataManager.save();
            }
            lastDays.put(world.getName(), day);
        }

        double radius = configManager.getShieldRadius();
        if (radius <= 0) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!dataManager.isShieldEnabled(player.getUniqueId())) continue;

            if (configManager.isMobKnockbackEnabled()) {
                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
                    if (!(entity instanceof Monster)) continue;
                    Vector fromPlayer = entity.getLocation().toVector().subtract(player.getLocation().toVector());
                    if (fromPlayer.lengthSquared() == 0) continue;

                    Vector horizontal = fromPlayer.clone();
                    horizontal.setY(0);
                    horizontal.normalize().multiply(configManager.getMobKnockbackPower());

                    double height = configManager.getDouble("mobs.knockback-height", configManager.getMobKnockbackPower() * 0.5);
                    Vector velocity = horizontal.clone();
                    velocity.setY(height);
                    entity.setVelocity(velocity);
                }
            }

            if (configManager.isArrowDeflectEnabled()) {
                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
                    if (!(entity instanceof Arrow)) continue;
                    if (entity.hasMetadata("buildershield_deflected")) continue;
                    Arrow arrow = (Arrow) entity;
                    if (!(arrow.getShooter() instanceof Skeleton)) continue;
                    Skeleton skeleton = (Skeleton) arrow.getShooter();
                    Vector direction = skeleton.getLocation().toVector().subtract(arrow.getLocation().toVector()).normalize();
                    double speed = configManager.getDouble("arrows.deflect-power", 2.0);
                    arrow.setVelocity(direction.multiply(speed));
                    arrow.setMetadata("buildershield_deflected", new FixedMetadataValue(plugin, true));
                }
            }
        }
    }
}