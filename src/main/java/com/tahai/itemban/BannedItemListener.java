package com.tahai.itemban;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class BannedItemListener implements Listener {

    private final Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemBan");
    private final DataManager dataManager = new DataManager();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !hasBannedNbtKey(item)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();

        if (hand == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(null);
        } else if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(null);
        }

        player.sendMessage(getBannedMessage());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPickup(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (!hasBannedNbtKey(item)) {
            return;
        }

        event.setCancelled(true);
        event.getItem().remove();

        if (event.getEntity() instanceof Player player) {
            player.sendMessage(getBannedMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();
        boolean removed = false;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (hasBannedNbtKey(item)) {
                inv.setItem(i, null);
                removed = true;
            }
        }

        if (removed) {
            player.sendMessage(getBannedMessage());
        }
    }

    private boolean hasBannedNbtKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (NamespacedKey key : pdc.getKeys()) {
            if (dataManager.contains(key.toString()) || dataManager.contains(key.getKey())) {
                return true;
            }
        }
        return false;
    }

    private String getBannedMessage() {
        String msg = plugin.getConfig().getString("messages.item-banned", "该物品已被禁止！");
        return msg == null ? "" : msg;
    }
}