package com.tahai.backpackban;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import java.util.Set;

public class BackpackListener implements Listener {

    private final StateManager stateManager;

    public BackpackListener() {
        this.stateManager = new StateManager();
    }

    public BackpackListener(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!stateManager.isEnabled()) return;
        Player player = event.getPlayer();
        if (player.isOp()) return;
        player.getInventory().clear();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!stateManager.isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.isOp()) return;
        Inventory inv = event.getInventory();
        if (!(inv instanceof PlayerInventory)) return;
        if (!inv.equals(player.getInventory())) return;
        int slot = event.getSlot();
        if (slot >= 9 && slot <= 39) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!stateManager.isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.isOp()) return;
        Inventory inv = event.getInventory();
        if (!(inv instanceof PlayerInventory)) return;
        if (!inv.equals(player.getInventory())) return;
        Set<Integer> slots = event.getInventorySlots();
        for (int slot : slots) {
            if (slot >= 9 && slot <= 39) {
                event.setCancelled(true);
                return;
            }
        }
    }
}