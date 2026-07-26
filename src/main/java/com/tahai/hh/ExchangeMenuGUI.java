package com.tahai.hh;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ExchangeMenuGUI implements InventoryHolder, Listener {
    private final Inventory inventory;
    private final Plugin plugin;

    private ExchangeMenuGUI(Plugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 27, "兑换菜单");
        initializeItems();
    }

    private void initializeItems() {
        inventory.setItem(11, createExchangeItem(100, 1));
        inventory.setItem(13, createExchangeItem(1000, 10));
        inventory.setItem(15, createExchangeItem(10000, 100));
        inventory.setItem(17, createExchangeItem(100000, 1000));
    }

    private ItemStack createExchangeItem(int goldCost, int pointReward) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "兑换 " + goldCost + " 金币 → " + pointReward + " 点券");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "消耗 " + goldCost + " 金币");
        lore.add(ChatColor.GRAY + "获得 " + pointReward + " 点券");
        meta.setLore(lore);

        // Store exchange values in persistent data for easy retrieval
        NamespacedKey goldKey = new NamespacedKey(plugin, "goldCost");
        NamespacedKey pointKey = new NamespacedKey(plugin, "pointReward");
        meta.getPersistentDataContainer().set(goldKey, PersistentDataType.INTEGER, goldCost);
        meta.getPersistentDataContainer().set(pointKey, PersistentDataType.INTEGER, pointReward);

        item.setItemMeta(meta);
        return item;
    }

    public static Inventory create(Plugin plugin) {
        ExchangeMenuGUI gui = new ExchangeMenuGUI(plugin);
        return gui.getInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}