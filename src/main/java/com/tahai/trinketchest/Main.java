package com.tahai.trinketchest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin {

    private final Map<Player, PermissionAttachment> attachments = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("sp").setExecutor(new SpCommand());
        getCommand("sp").setTabCompleter(new SpCommand());
        getServer().getPluginManager().registerEvents(new ClickListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        for (Map.Entry<Player, PermissionAttachment> entry : attachments.entrySet()) {
            entry.getKey().removeAttachment(entry.getValue());
        }
        attachments.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            GUIHolder holder = new GUIHolder();
            List<ItemStack> items = holder.loadItemsFromPDC(player);
            int size = holder.loadSizeFromPDC(player);
            if (items != null) {
                holder.saveItemsToPDC(player, items, size);
            }
        }
    }
}