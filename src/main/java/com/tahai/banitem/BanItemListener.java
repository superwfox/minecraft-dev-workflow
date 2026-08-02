package com.tahai.banitem;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BanItemListener implements Listener {
    private final DatabaseManager databaseManager;

    public BanItemListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (databaseManager.isBanned(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.AQUA + "该物品已被禁用！");
        }
    }
}