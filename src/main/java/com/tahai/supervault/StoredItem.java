package com.tahai.supervault;

import org.bukkit.inventory.ItemStack;

public class StoredItem {
    private final ItemStack item;
    private final int slot;

    public StoredItem(ItemStack item, int slot) {
        this.item = item;
        this.slot = slot;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }
}