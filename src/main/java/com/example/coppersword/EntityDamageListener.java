package com.example.coppersword;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

public class EntityDamageListener implements Listener {

    private final ConfigManager configManager;
    private final CooldownManager cooldownManager;

    public EntityDamageListener() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CopperSword");
        configManager = new ConfigManager();
        cooldownManager = new CooldownManager();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (configManager == null || cooldownManager == null) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (attacker.getInventory().getItemInMainHand().getType() != Material.STONE_SWORD) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        if (cooldownManager.isOnCooldown(attacker)) {
            attacker.sendMessage(configManager.getMessage("cooldown-message"));
            event.setCancelled(true);
            return;
        }

        double healthPercent = (target.getHealth() / target.getMaxHealth()) * 100.0;
        int threshold = configManager.getStarRangeMin();
        if (healthPercent < threshold) {
            target.setHealth(0);
            cooldownManager.setCooldown(attacker, configManager.getCooldownSeconds() * 1000L);
            attacker.sendMessage(configManager.getMessage("kill-message"));
            event.setCancelled(true);
        }
    }
}