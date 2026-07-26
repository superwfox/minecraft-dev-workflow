package com.tahai.hh;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class GuiHolder implements Listener, InventoryHolder {
    private final Inventory inv;
    private final NamespacedKey costKey;
    private final NamespacedKey rewardKey;

    public GuiHolder() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Hh");
        costKey = new NamespacedKey(plugin, "cost");
        rewardKey = new NamespacedKey(plugin, "reward");
        inv = Bukkit.createInventory(this, 9, "金币兑换点券");

        int[] costs = {100, 1000, 10000, 100000};
        int[] rewards = {1, 10, 100, 1000};
        String[] names = {
                "100金币换1点券",
                "1000金币换10点券",
                "10000金币换100点券",
                "100000金币换1000点券"
        };
        Material[] materials = {Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT, Material.EMERALD};
        int[] amounts = {1, 10, 64, 64};

        for (int i = 0; i < 4; i++) {
            ItemStack item = new ItemStack(materials[i], amounts[i]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(names[i]);

            List<String> lore = new ArrayList<>();
            lore.add("所需金币: " + costs[i]);
            lore.add("获得点券: " + rewards[i]);
            meta.setLore(lore);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(costKey, PersistentDataType.INTEGER, costs[i]);
            pdc.set(rewardKey, PersistentDataType.INTEGER, rewards[i]);

            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public void open(Player player) {
        player.openInventory(inv);
    }
}