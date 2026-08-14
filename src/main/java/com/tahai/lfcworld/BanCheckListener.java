package com.tahai.lfcworld;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class BanCheckListener implements Listener {

    private final BanManager banManager;
    private final JavaPlugin plugin;

    public BanCheckListener(BanManager banManager) {
        this.banManager = banManager;
        this.plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("LfcWorld");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!banManager.isEnabled()) return;
        Player player = event.getPlayer();
        banManager.handleBannedItems(player, player.getWorld().getName());
        if (isEndWorld(player.getWorld().getName())) {
            removeElytra(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!banManager.isEnabled()) return;
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        banManager.handleBannedItems(player, worldName);
        if (isEndWorld(worldName)) {
            removeElytra(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!banManager.isEnabled()) return;
        if (event.getWhoClicked() instanceof Player player) {
            scheduleCheck(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!banManager.isEnabled()) return;
        scheduleCheck(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!banManager.isEnabled()) return;
        scheduleCheck(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!banManager.isEnabled()) return;
        scheduleCheck(event.getPlayer());
    }

    private void scheduleCheck(Player player) {
        if (plugin == null) {
            banManager.handleBannedItems(player, player.getWorld().getName());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                banManager.handleBannedItems(player, player.getWorld().getName());
            }
        });
    }

    private boolean isEndWorld(String worldName) {
        return plugin != null && plugin.getConfig().getStringList("end-worlds").contains(worldName);
    }

    private void removeElytra(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() != Material.ELYTRA) return;
        player.getInventory().setChestplate(null);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(chestplate);
        if (!leftover.isEmpty()) {
            World world = player.getWorld();
            for (ItemStack item : leftover.values()) {
                world.dropItemNaturally(player.getLocation(), item);
            }
        }
    }
}