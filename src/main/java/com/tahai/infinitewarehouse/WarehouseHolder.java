package com.tahai.infinitewarehouse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class WarehouseHolder implements InventoryHolder, Listener {

    private static final int SIZE = 54;
    private static final String TITLE = "无限仓库";

    private Inventory inventory;

    public static Inventory createInventory(Player player, DataManager dataManager) {
        WarehouseHolder holder = new WarehouseHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, TITLE);
        Plugin plugin = Bukkit.getPluginManager().getPlugin("InfiniteWarehouse");
        NamespacedKey countKey = new NamespacedKey(plugin, "count");
        ItemStack[] items = dataManager.getWarehouse(player);
        if (items != null) {
            for (int i = 0; i < Math.min(items.length, SIZE); i++) {
                if (items[i] != null && items[i].getType() != Material.AIR) {
                    inv.setItem(i, prepareItem(items[i], countKey));
                }
            }
        }
        holder.inventory = inv;
        return inv;
    }

    private static ItemStack prepareItem(ItemStack item, NamespacedKey countKey) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        int realCount = copy.getAmount();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(countKey, PersistentDataType.INTEGER)) {
                realCount = pdc.get(countKey, PersistentDataType.INTEGER);
            }
            pdc.set(countKey, PersistentDataType.INTEGER, realCount);
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            } else {
                lore = new ArrayList<>(lore);
            }
            lore.removeIf(line -> line.startsWith(ChatColor.GRAY + "数量: "));
            lore.add(ChatColor.GRAY + "数量: " + realCount);
            meta.setLore(lore);
            copy.setItemMeta(meta);
        }
        copy.setAmount(1);
        return copy;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}