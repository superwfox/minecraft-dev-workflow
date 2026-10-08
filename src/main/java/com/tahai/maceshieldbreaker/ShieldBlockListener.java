package com.tahai.maceshieldbreaker;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerShieldBlockEvent;
import org.bukkit.plugin.Plugin;

public class ShieldBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onShieldBlock(PlayerShieldBlockEvent event) {
        Entity damager = event.getDamageSource().getCausingEntity();
        if (!(damager instanceof Player attacker)) {
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("MaceShieldBreaker");
        if (plugin == null) {
            return;
        }

        WhitelistManager whitelist = new WhitelistManager(plugin);
        whitelist.load();
        if (!whitelist.isWhitelisted(attacker.getName())) {
            return;
        }

        if (attacker.getInventory().getItemInMainHand().getType() != Material.MACE) {
            return;
        }

        event.setBlocked(false);

        Entity victim = event.getEntity();
        if (victim instanceof Player player) {
            player.setCooldown(Material.SHIELD, 32);
        }
    }
}