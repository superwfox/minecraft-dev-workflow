package com.tahai.infinitewarehouse;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class RightClickListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !isWarehouseItem(item)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        new WarehouseHolder().open(player);
    }

    private boolean isWarehouseItem(ItemStack item) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("InfiniteWarehouse");
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        Material material = Material.getMaterial(plugin.getConfig().getString("warehouse.item.material", "CHEST"));
        if (item.getType() != material) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        String name = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("warehouse.item.name", ""));
        if (name.isEmpty() || !meta.hasDisplayName() || !meta.getDisplayName().equals(name)) {
            return false;
        }
        List<String> lore = plugin.getConfig().getStringList("warehouse.item.lore");
        if (!lore.isEmpty()) {
            if (!meta.hasLore()) {
                return false;
            }
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
            if (!meta.getLore().equals(lore)) {
                return false;
            }
        } else if (meta.hasLore()) {
            return false;
        }
        return true;
    }
}