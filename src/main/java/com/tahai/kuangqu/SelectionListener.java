package com.tahai.kuangqu;

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

public class SelectionListener implements Listener {

    private final SelectionManager selectionManager = new SelectionManager();
    private final Plugin plugin;

    public SelectionListener() {
        this.plugin = Bukkit.getPluginManager().getPlugin("Kuangqu");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (plugin == null) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.WOODEN_SHOVEL) return;

        Action action = event.getAction();
        Location location = event.getClickedBlock().getLocation();

        if (action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            selectionManager.setPos1(player.getUniqueId(), location);
            String msg = plugin.getConfig().getString("messages.pos1-set", "&e已设置点A: %coord%");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    msg.replace("%player%", player.getName())
                            .replace("%coord%", location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ())));
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            selectionManager.setPos2(player.getUniqueId(), location);
            String msg = plugin.getConfig().getString("messages.pos2-set", "&e已设置点B: %coord%");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    msg.replace("%player%", player.getName())
                            .replace("%coord%", location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ())));
        }
    }
}