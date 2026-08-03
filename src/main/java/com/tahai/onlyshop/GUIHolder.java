package com.tahai.onlyshop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIHolder implements Listener, InventoryHolder {

    private final Inventory inventory;
    private final String guiType;
    private final int itemIndex;

    private GUIHolder(String title, String guiType, int itemIndex) {
        int size = "player".equals(guiType) ? 54 : 27;
        this.inventory = Bukkit.createInventory(this, size, title);
        this.guiType = guiType;
        this.itemIndex = itemIndex;
    }

    public static GUIHolder createPlayerShop() {
        return new GUIHolder("只限商店", "player", -1);
    }

    public static GUIHolder createAdminManage() {
        return new GUIHolder("商店管理", "admin", -1);
    }

    public static GUIHolder createItemSetting(int itemIndex) {
        return new GUIHolder("物品设置", "item", itemIndex);
    }

    public void open(Player p) {
        p.openInventory(inventory);
    }

    public String getGuiType() {
        return guiType;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}