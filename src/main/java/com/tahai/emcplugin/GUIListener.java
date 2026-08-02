package com.tahai.emcplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        GUIHolder holder = (GUIHolder) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;
        event.setCancelled(true);

        switch (holder.getType()) {
            case MAIN_MENU:
                if (slot == GUIHolder.SELL_SLOT) {
                    new GUIHolder().createSellGUI(holder.getDataManager()).open(player);
                } else if (slot == GUIHolder.SHOP_SLOT) {
                    new GUIHolder().createShopGUI(holder.getDataManager()).open(player);
                }
                break;
            case SELL:
                if (slot == GUIHolder.SELL_CONFIRM_SLOT) {
                    sellItems(holder, player);
                } else {
                    event.setCancelled(false);
                }
                break;
            case SHOP:
                ItemStack current = event.getCurrentItem();
                if (current != null) {
                    buyItem(holder, player, current);
                }
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof GUIHolder) {
            GUIHolder holder = (GUIHolder) event.getInventory().getHolder();
            if (holder.getType() == GUIHolder.GUIType.SELL) {
                Player player = (Player) event.getPlayer();
                Inventory inv = holder.getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    if (i == GUIHolder.SELL_CONFIRM_SLOT) continue;
                    ItemStack item = inv.getItem(i);
                    if (item != null) {
                        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                        for (ItemStack left : leftovers.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), left);
                        }
                        inv.setItem(i, null);
                    }
                }
            }
        }
    }

    private void sellItems(GUIHolder holder, Player player) {
        DataManager dm = holder.getDataManager();
        Inventory inv = holder.getInventory();
        double total = 0;
        List<Integer> validSlots = new ArrayList<>();

        for (int i = 0; i < inv.getSize(); i++) {
            if (i == GUIHolder.SELL_CONFIRM_SLOT) continue;
            ItemStack item = inv.getItem(i);
            if (item != null) {
                double value = dm.getValue(item.getType().name());
                if (value <= 0) {
                    player.sendMessage(ChatColor.AQUA + item.getType().name() + " 无法出售");
                } else {
                    total += value * item.getAmount();
                    validSlots.add(i);
                }
            }
        }

        if (validSlots.isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "没有可出售的物品");
            return;
        }

        dm.addPoints(player.getUniqueId(), total);
        for (int slot : validSlots) {
            inv.setItem(slot, null);
        }
        dm.save();
        player.sendMessage(ChatColor.YELLOW + "出售成功，获得 " + String.format("%.2f", total) + " 点数");
    }

    private void buyItem(GUIHolder holder, Player player, ItemStack item) {
        DataManager dm = holder.getDataManager();
        String name = item.getType().name();
        double price = dm.getValue(name);
        if (price <= 0) {
            player.sendMessage(ChatColor.AQUA + "该物品不可购买");
            return;
        }
        if (dm.getPoints(player.getUniqueId()) < price) {
            player.sendMessage(ChatColor.AQUA + "点数不足，需要 " + String.format("%.2f", price) + " 点数");
            return;
        }
        dm.removePoints(player.getUniqueId(), price);
        player.getInventory().addItem(item.clone());
        dm.save();
        player.sendMessage(ChatColor.YELLOW + "购买成功，消耗 " + String.format("%.2f", price) + " 点数");
    }
}