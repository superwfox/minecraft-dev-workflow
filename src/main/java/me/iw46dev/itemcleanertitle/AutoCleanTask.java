package me.iw46dev.itemcleanertitle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoCleanTask extends BukkitRunnable {

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType() == EntityType.DROPPED_ITEM) {
                    entity.remove();
                }
            }
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemCleanerTitle");
        if (plugin == null) return;
        String message = plugin.getConfig().getString("auto-clean.message", "&6地上物品已清理！");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}