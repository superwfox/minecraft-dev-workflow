package com.tahai.joindiamond;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class JoinListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        ItemStack diamond = new ItemStack(Material.DIAMOND, 1);
        Map<Integer, ItemStack> leftover = event.getPlayer().getInventory().addItem(diamond);
        for (ItemStack remaining : leftover.values()) {
            event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), remaining);
        }
    }
}