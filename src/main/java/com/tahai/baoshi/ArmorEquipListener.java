package com.tahai.baoshi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class ArmorEquipListener implements Listener {

    private final DataManager dataManager;
    private final Plugin plugin;

    public ArmorEquipListener(DataManager dataManager, Plugin plugin) {
        this.dataManager = dataManager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateMaxHealth(player), 1L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateMaxHealth(player), 1L);
        }
    }

    private void updateMaxHealth(Player player) {
        double baseHealth = 20.0;
        double bonus = 0.0;

        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            UUID uuid = NbtUtil.getUUID(item);
            if (uuid == null) continue;

            String type = dataManager.getGemType(uuid);
            if ("xuanbingshi".equals(type)) {
                int level = dataManager.getGemLevel(uuid);
                bonus += level * 2.0;
            }
        }

        double newMax = baseHealth + bonus;
        player.setMaxHealth(newMax);
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }
}