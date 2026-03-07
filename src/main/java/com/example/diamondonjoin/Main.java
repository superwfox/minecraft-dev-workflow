package com.example.diamondonjoin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiamondOnJoin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DiamondOnJoin 插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("DiamondOnJoin 插件已禁用。");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack diamond = new ItemStack(Material.DIAMOND, 1);
        player.getInventory().addItem(diamond);
        player.sendMessage("欢迎回来！你获得了一颗钻石。");
    }
}