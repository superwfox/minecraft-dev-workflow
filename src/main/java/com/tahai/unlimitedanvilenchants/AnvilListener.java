package com.tahai.unlimitedanvilenchants;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AnvilListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = (AnvilInventory) event.getInventory();
        ItemStack first = inv.getFirstItem();
        ItemStack second = inv.getSecondItem();
        ItemStack originalResult = event.getResult();
        if (first == null || second == null || originalResult == null
                || first.getType() == Material.AIR || second.getType() == Material.AIR
                || originalResult.getType() == Material.AIR) {
            return;
        }

        int originalCost = inv.getRepairCost();
        Map<Enchantment, Integer> combined = combineEnchantments(first, second);

        if (reachedLimit(combined)) {
            event.setResult(null);
            inv.setRepairCost(0);
            HumanEntity viewer = event.getView().getPlayer();
            viewer.sendMessage(ChatColor.AQUA + "已达附魔等级上限！");
            return;
        }

        ItemStack result = originalResult.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            for (Enchantment ench : new ArrayList<>(meta.getEnchants().keySet())) {
                meta.removeEnchant(ench);
            }
            result.setItemMeta(meta);
        }
        result.addUnsafeEnchantments(combined);
        event.setResult(result);

        inv.setRepairCost(exceedsVanillaMax(combined) ? 40 : originalCost);
    }

    private Map<Enchantment, Integer> combineEnchantments(ItemStack first, ItemStack second) {
        Map<Enchantment, Integer> enchants = new HashMap<>(first.getEnchantments());
        for (Map.Entry<Enchantment, Integer> entry : second.getEnchantments().entrySet()) {
            Enchantment enchant = entry.getKey();
            int secondLevel = entry.getValue();
            int firstLevel = enchants.getOrDefault(enchant, 0);
            if (firstLevel > 0) {
                int newLevel = firstLevel == secondLevel ? firstLevel + 1 : Math.max(firstLevel, secondLevel);
                enchants.put(enchant, newLevel);
            } else if ((first.getType() == Material.BOOK || first.getType() == Material.ENCHANTED_BOOK
                    || enchant.canEnchantItem(first)) && !hasConflict(enchants.keySet(), enchant)) {
                enchants.put(enchant, secondLevel);
            }
        }
        return enchants;
    }

    private boolean hasConflict(Set<Enchantment> existing, Enchantment candidate) {
        for (Enchantment ench : existing) {
            if (ench.conflictsWith(candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean reachedLimit(Map<Enchantment, Integer> enchants) {
        for (int level : enchants.values()) {
            if (level >= 32767) {
                return true;
            }
        }
        return false;
    }

    private boolean exceedsVanillaMax(Map<Enchantment, Integer> enchants) {
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getValue() > entry.getKey().getMaxLevel()) {
                return true;
            }
        }
        return false;
    }
}