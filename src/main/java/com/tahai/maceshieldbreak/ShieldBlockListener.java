package com.tahai.maceshieldbreak;

import io.papermc.paper.event.player.PlayerShieldBlockEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ShieldBlockListener implements Listener {

    private final MaceWhitelistManager whitelist;

    public ShieldBlockListener(MaceWhitelistManager whitelist) {
        this.whitelist = whitelist;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShieldBlock(PlayerShieldBlockEvent event) {
        Entity blockerEntity = event.getEntity();
        if (!(blockerEntity instanceof Player blocker)) {
            return;
        }

        DamageSource source = event.getDamageSource();
        if (source == null) {
            return;
        }

        Entity damager = source.getCausingEntity();
        if (!(damager instanceof Player attacker)) {
            return;
        }

        if (!whitelist.contains(attacker.getName())) {
            return;
        }

        if (attacker.getInventory().getItemInMainHand().getType() != Material.MACE) {
            return;
        }

        event.setCancelled(true);
        blocker.setCooldown(Material.SHIELD, 100);
        blocker.getWorld().playSound(blocker.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
    }
}