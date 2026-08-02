package com.tahai.emcplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIHolder implements Listener, InventoryHolder {

    public enum GUIType {
        MAIN_MENU,
        SELL,
        SHOP
    }

    public static final int SELL_SLOT = 11;
    public static final int SHOP_SLOT = 15;
    public static final int SELL_CONFIRM_SLOT = 22;

    private final GUIType type;
    private final Inventory inventory;
    private final DataManager dataManager;

    private GUIHolder(GUIType type, DataManager dataManager, String title, int size) {
        this.type = type;
        this.dataManager = dataManager;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public GUIType getType() {
        return type;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public static GUIHolder createMainMenu(DataManager dataManager) {
        GUIHolder holder = new GUIHolder(GUIType.MAIN_MENU, dataManager, "主菜单", 27);
        holder.inventory.setItem(SELL_SLOT, createItem(Material.CHEST,
                ChatColor.YELLOW + "物品卖出",
                ChatColor.GRAY + "将物品放入后卖出"));
        holder.inventory.setItem(SHOP_SLOT, createItem(Material.BOOK,
                ChatColor.YELLOW + "商品目录",
                ChatColor.GRAY + "查看可购买的商品"));
        return holder;
    }

    public static GUIHolder createSellGUI(DataManager dataManager) {
        GUIHolder holder = new GUIHolder(GUIType.SELL, dataManager, "卖出", 27);
        holder.inventory.setItem(SELL_CONFIRM_SLOT, createItem(Material.EMERALD,
                ChatColor.YELLOW + "确认卖出",
                ChatColor.GRAY + "卖出所有放入的物品"));
        return holder;
    }

    public static GUIHolder createShopGUI(DataManager dataManager) {
        GUIHolder holder = new GUIHolder(GUIType.SHOP, dataManager, "商品目录", 54);
        int slot = 0;
        for (Material material : Material.values()) {
            if (slot >= 54) {
                break;
            }
            if (!material.isItem()) {
                continue;
            }
            double value = dataManager.getValue(material.name());
            if (value > 0) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                meta.setLore(Arrays.asList(
                        ChatColor.YELLOW + "价值: " + formatValue(value) + " 点",
                        ChatColor.GRAY + "点击购买"));
                item.setItemMeta(meta);
                holder.inventory.setItem(slot++, item);
            }
        }
        return holder;
    }

    private static String formatValue(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}