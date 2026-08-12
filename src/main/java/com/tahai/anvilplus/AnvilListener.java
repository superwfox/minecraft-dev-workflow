package com.tahai.anvilplus;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack result = event.getResult();
        if (result == null) {
            return;
        }

        ItemStack target = inv.getItem(0);
        ItemStack sacrifice = inv.getItem(1);

        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                int targetLevel = (target != null) ? target.getEnchantmentLevel(enchantment) : 0;
                int sacrificeLevel = (sacrifice != null) ? sacrifice.getEnchantmentLevel(enchantment) : 0;

                int rawLevel;
                if (targetLevel == 0) {
                    rawLevel = sacrificeLevel;
                } else if (sacrificeLevel == 0) {
                    rawLevel = targetLevel;
                } else {
                    rawLevel = (targetLevel == sacrificeLevel) ? targetLevel + 1 : Math.max(targetLevel, sacrificeLevel);
                }

                if (rawLevel > 0) {
                    meta.removeEnchant(enchantment);
                    meta.addEnchant(enchantment, rawLevel, true);
                }
            }
            result.setItemMeta(meta);
            event.setResult(result);
        }

        int cost = inv.getRepairCost();
        if (cost >= 40) {
            inv.setRepairCost(39);
        }
    }
}