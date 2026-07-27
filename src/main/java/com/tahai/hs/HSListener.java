package com.tahai.hs;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class HSListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RecycleGui)) return;

        int slot = event.getSlot();
        if (slot == RecycleGui.BUTTON_SLOT && event.getView().getTopInventory().equals(event.getClickedInventory())) {
            event.setCancelled(true);
            RecycleGui gui = (RecycleGui) event.getInventory().getHolder();
            if (event.getWhoClicked() instanceof Player) {
                gui.recycle((Player) event.getWhoClicked());
            }
            return;
        }

        // 禁止任何对按钮物品的间接移动（例如热键交换）
        if (event.getRawSlot() == RecycleGui.BUTTON_SLOT && event.getView().getTopInventory().equals(event.getClickedInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof RecycleGui)) return;

        // 禁止拖拽包含按钮槽位的操作
        if (event.getInventorySlots().contains(RecycleGui.BUTTON_SLOT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof RecycleGui)) return;

        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();

        // 清空按钮槽位防止把按钮物品丢给玩家
        inv.setItem(RecycleGui.BUTTON_SLOT, null);

        // 归还剩余物品
        for (int i = 0; i < inv.getSize(); i++) {
            if (i == RecycleGui.BUTTON_SLOT) continue;
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    for (ItemStack drop : leftover.values()) {
                        player.getWorld().dropItem(player.getLocation(), drop);
                    }
                }
                inv.setItem(i, null);
            }
        }
    }
}