package com.tahai.spaccessory;

import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DeathListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Boolean keepInventory = player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
        if (keepInventory != null && keepInventory) {
            return;
        }

        List<ItemStack> accessories = PlayerDataUtil.loadItemsFromPDC(player);
        if (accessories.isEmpty()) {
            return;
        }

        for (ItemStack item : accessories) {
            if (item != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

        PlayerDataUtil.saveItemsToPDC(player, new ArrayList<>());
    }
}