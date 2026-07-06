package com.tahai.spaccessory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GuiHolder implements InventoryHolder, Listener {

    private Inventory inventory;
    private boolean[] enabledSlots;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static Inventory createInventory(Player player, int size) {
        GuiHolder holder = new GuiHolder();
        holder.inventory = Bukkit.createInventory(holder, size, ChatColor.BLUE + "饰品");
        holder.enabledSlots = new boolean[size];
        return holder.inventory;
    }

    public void setSlotEnabled(int slot, boolean enabled) {
        if (enabledSlots != null && slot >= 0 && slot < enabledSlots.length) {
            enabledSlots[slot] = enabled;
        }
    }

    public boolean isSlotEnabled(int slot) {
        return enabledSlots != null && slot >= 0 && slot < enabledSlots.length && enabledSlots[slot];
    }

    public List<ItemStack> getAccessoryItems() {
        List<ItemStack> items = new ArrayList<>();
        if (inventory == null || enabledSlots == null) {
            return items;
        }
        for (int i = 0; i < enabledSlots.length; i++) {
            if (enabledSlots[i]) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    public void open(Player player) {
        if (inventory != null) {
            player.openInventory(inventory);
        }
    }
}