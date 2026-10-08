package com.tahai.maceshieldbreak;

import io.papermc.paper.event.player.PlayerShieldBlockEvent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ShieldBlockListener implements Listener {

    private final WhitelistManager whitelistManager;

    public ShieldBlockListener(WhitelistManager whitelistManager) {
        this.whitelistManager = whitelistManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShieldBlock(PlayerShieldBlockEvent event) {
        Entity attackerEntity = event.getBlockedEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) {
            return;
        }

        ItemStack mainHand = attacker.getEquipment() == null
                ? null
                : attacker.getEquipment().getItemInMainHand();
        if (mainHand == null || mainHand.getType() != Material.MACE) {
            return;
        }

        if (!whitelistManager.isWhitelisted(attacker.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        Player blocker = event.getEntity();
        blocker.setCooldown(Material.SHIELD, 100);
    }
}