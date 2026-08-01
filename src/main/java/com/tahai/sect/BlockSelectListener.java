package com.tahai.sect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockSelectListener implements Listener {

    private final Map<UUID, Location[]> selections = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack item;
        if (hand == EquipmentSlot.OFF_HAND) {
            item = player.getInventory().getItemInOffHand();
        } else {
            item = player.getInventory().getItemInMainHand();
        }

        if (item == null || item.getType() != Material.GRASS_BLOCK) {
            return;
        }

        event.setCancelled(true);
        Location location = event.getClickedBlock().getLocation();
        Location[] points = selections.computeIfAbsent(player.getUniqueId(), k -> new Location[2]);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            points[0] = location;
            player.sendMessage(ChatColor.GRAY + "已设置领地A点: " + ChatColor.YELLOW
                    + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            points[1] = location;
            player.sendMessage(ChatColor.GRAY + "已设置领地B点: " + ChatColor.YELLOW
                    + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        }
    }

    public Location[] getSelection(UUID uuid) {
        return selections.get(uuid);
    }
}