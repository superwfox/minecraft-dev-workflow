package com.tahai.onlyshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import java.util.List;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;

public class GUIListener implements Listener {
    private final DataManager dataManager;

    public GUIListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getType() == InventoryType.ANVIL && event.getRawSlot() == 2) {
            handleAnvilClick(event);
            return;
        }
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        event.setCancelled(true);
        if (event.getRawSlot() >= event.getInventory().getSize()) return;
        GUIHolder holder = (GUIHolder) event.getInventory().getHolder();
        String type = holder.getGuiType();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (type.equals("player")) {
            handlePlayerClick(event, holder, player, slot);
        } else if (type.equals("admin")) {
            handleAdminClick(event, holder, player, slot);
        }
    }

    private void handlePlayerClick(InventoryClickEvent event, GUIHolder holder, Player player, int slot) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        List<String> items = dataManager.getItemList();
        if (slot < 0 || slot >= items.size()) return;
        String itemId = items.get(slot);
        if (!player.hasPermission("shop.buy")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限购买物品。");
            return;
        }
        double price = dataManager.getPrice(itemId);
        String currency = dataManager.getCurrency(itemId);
        int limit = dataManager.getLimit(itemId);
        String uuid = player.getUniqueId().toString();
        if (limit > 0 && dataManager.getPurchaseCount(uuid, itemId) >= limit) {
            player.sendMessage(ChatColor.AQUA + "你已达到该物品的购买上限。");
            return;
        }
        int amount;
        if (event.isShiftClick() && event.isLeftClick()) {
            amount = 64;
        } else if (event.isLeftClick()) {
            amount = 1;
        } else if (event.isRightClick()) {
            amount = 10;
        } else {
            return;
        }
        double cost = price * amount;
        boolean success;
        if (currency.equalsIgnoreCase("Vault")) {
            success = processVault(player, cost);
        } else if (currency.equalsIgnoreCase("PlayerPoints")) {
            success = processPlayerPoints(player, cost);
        } else {
            player.sendMessage(ChatColor.AQUA + "未知货币类型 " + currency + "。");
            return;
        }
        if (success) {
            int count = dataManager.getPurchaseCount(uuid, itemId);
            dataManager.setPurchaseCount(uuid, itemId, count + amount);
            dataManager.save();
            ItemStack give = item.clone();
            give.setAmount(amount);
            player.getInventory().addItem(give);
            player.sendMessage(ChatColor.YELLOW + "购买成功！");
        } else {
            player.sendMessage(ChatColor.AQUA + "货币不足或扣款失败。");
        }
    }

    private boolean processVault(Player player, double cost) {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            player.sendMessage(ChatColor.AQUA + "经济系统未启用。");
            return false;
        }
        Economy economy = rsp.getProvider();
        if (!economy.has(player, cost)) return false;
        EconomyResponse resp = economy.withdrawPlayer(player, cost);
        return resp.transactionSuccess();
    }

    private boolean processPlayerPoints(Player player, double cost) {
        PlayerPoints plugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (plugin == null) {
            player.sendMessage(ChatColor.AQUA + "PlayerPoints 插件未启用。");
            return false;
        }
        PlayerPointsAPI api = plugin.getAPI();
        int points = api.look(player.getUniqueId());
        int intCost = (int) cost;
        if (cost != intCost || points < intCost) return false;
        return api.take(player.getUniqueId(), intCost);
    }

    private void handleAdminClick(InventoryClickEvent event, GUIHolder holder, Player player, int slot) {
        if (!player.hasPermission("shopadmin")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限管理商店。");
            return;
        }
        ItemStack cursor = event.getCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            String itemId = cursor.getType().toString() + ":" + cursor.getDurability();
            dataManager.updateItem(itemId, 0.0, "Vault", 0);
            dataManager.save();
            event.setCursor(null);
            player.sendMessage(ChatColor.YELLOW + "已添加商品 " + itemId + "，请前往设置价格。");
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        List<String> items = dataManager.getItemList();
        if (slot >= 0 && slot < items.size()) {
            GUIHolder setting = holder.createItemSetting(slot);
            setting.open(player);
        }
    }

    private void handleAnvilClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("shopadmin")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限管理商店。");
            return;
        }
        ItemStack item0 = event.getView().getItem(0);
        if (item0 == null || item0.getType() == Material.AIR) return;
        String itemId = item0.getType().toString() + ":" + item0.getDurability();
        if (!dataManager.getItemList().contains(itemId)) {
            player.sendMessage(ChatColor.AQUA + "该商品不存在。");
            return;
        }
        String text = event.getView().getRenameText();
        if (text == null || text.isEmpty()) return;
        String[] parts = text.split(" ");
        if (parts.length != 3) {
            player.sendMessage(ChatColor.AQUA + "格式错误，请使用：价格 货币 上限");
            return;
        }
        try {
            double price = Double.parseDouble(parts[0]);
            String currency = parts[1];
            int limit = Integer.parseInt(parts[2]);
            dataManager.updateItem(itemId, price, currency, limit);
            dataManager.save();
            player.sendMessage(ChatColor.YELLOW + "商品 " + itemId + " 已更新。");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.AQUA + "价格或上限不是有效数字。");
        }
    }
}