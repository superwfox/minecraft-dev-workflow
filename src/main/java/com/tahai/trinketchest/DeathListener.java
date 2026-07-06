package com.tahai.trinketchest;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class DeathListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Check keepInventory gamerule
        Boolean keepInventory = player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
        if (keepInventory != null && keepInventory) {
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TrinketChest");
        if (plugin == null) {
            return;
        }

        GUIHolder guiHolder = new GUIHolder();
        List<ItemStack> items = guiHolder.loadItemsFromPDC(player);
        int size = guiHolder.loadSizeFromPDC(player);

        // Drop items at death location
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null && !item.getType().isAir()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }

        // Clear trinket chest contents and save
        guiHolder.saveItemsToPDC(player, List.of(), size); // empty list

        // Remove sp permission (set to false via attachment)
        player.addAttachment(plugin, "sp", false);
    }
}