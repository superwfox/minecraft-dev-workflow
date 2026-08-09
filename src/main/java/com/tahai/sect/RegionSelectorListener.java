package com.tahai.sect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class RegionSelectorListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand == null || itemInMainHand.getType() != Material.GRASS_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Location location = event.getClickedBlock().getLocation();
        boolean isA = event.getAction() == Action.LEFT_CLICK_BLOCK;
        UUID uuid = player.getUniqueId();

        if (!storeSelection(uuid, location, isA)) {
            player.sendMessage(ChatColor.AQUA + "无法保存选区，请稍后再试。");
            return;
        }

        String pointName = isA ? "A" : "B";
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + pointName + ChatColor.YELLOW + "点已设置: " +
                ChatColor.GRAY + location.getWorld().getName() + " " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        event.setCancelled(true);
    }

    private boolean storeSelection(UUID uuid, Location location, boolean isA) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) return false;
        try {
            String key = "selections." + uuid.toString() + "." + (isA ? "a" : "b");
            String value = location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
            plugin.getConfig().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}