package com.tahai.hh;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;

public class MenuClickListener implements Listener {

    private final DataManager dataManager;
    private final Map<Integer, int[]> exchangeOptions = new HashMap<>();

    public MenuClickListener(DataManager dataManager) {
        this.dataManager = dataManager;
        // 定义兑换选项：slot -> [所需金币, 获得点券]
        exchangeOptions.put(11, new int[]{100, 10});
        exchangeOptions.put(13, new int[]{500, 55});
        exchangeOptions.put(15, new int[]{1000, 120});
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof ExchangeMenuGUI)) return;

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (!exchangeOptions.containsKey(rawSlot)) return;

        // 检查点击者是否为玩家
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        int[] option = exchangeOptions.get(rawSlot);
        int cost = option[0];
        int points = option[1];

        // 获取Vault经济
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            player.sendMessage(ChatColor.AQUA + "未检测到经济插件，请安装Vault兼容的经济插件。");
            return;
        }
        Economy economy = rsp.getProvider();

        if (!economy.has(player, cost)) {
            player.sendMessage(ChatColor.AQUA + "金币不足，需要 " + cost + " 金币。");
            return;
        }

        economy.withdrawPlayer(player, cost);
        dataManager.addPoints(player, points);
        player.sendMessage(ChatColor.YELLOW + "成功兑换 " + points + " 点券，消耗 " + cost + " 金币。");
    }
}