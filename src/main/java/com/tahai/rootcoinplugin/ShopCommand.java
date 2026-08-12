package com.tahai.rootcoinplugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopCommand implements CommandExecutor, TabCompleter {

    private static final Map<Material, Double> PRICES = Map.of(
        Material.IRON_INGOT, 5.0,
        Material.LAPIS_LAZULI, 5.0,
        Material.COAL, 5.0,
        Material.REDSTONE_BLOCK, 10.0,
        Material.DIAMOND, 15.0,
        Material.GOLD_BLOCK, 15.0,
        Material.NETHERITE_INGOT, 20.0
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行");
            return true;
        }
        if (!player.hasPermission("rootcoin.shop")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限使用商店");
            return true;
        }
        if (DataManager.getQQ(player.getUniqueId()) == null) {
            player.sendMessage(ChatColor.AQUA + "请先使用 /bind 绑定QQ后再购买");
            return true;
        }
        if (args.length == 0) {
            showMenu(player);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("buy")) {
            buyItem(player, args[1]);
            return true;
        }
        player.sendMessage(ChatColor.GRAY + "用法: /shop 或 /shop buy <物品>");
        return true;
    }

    private void showMenu(Player player) {
        player.sendMessage(ChatColor.YELLOW + "===== 系统商店 =====");
        player.sendMessage(ChatColor.GRAY + "输入 /shop buy <物品> 购买");
        PRICES.forEach((material, price) ->
            player.sendMessage(ChatColor.GRAY + " - " + material.name().toLowerCase() + ChatColor.YELLOW + " " + price + " 根号币")
        );
    }

    private void buyItem(Player player, String itemName) {
        Material material = null;
        for (Material m : PRICES.keySet()) {
            if (m.name().equalsIgnoreCase(itemName) || m.name().replace("_", "").equalsIgnoreCase(itemName.replace("_", ""))) {
                material = m;
                break;
            }
        }
        if (material == null) {
            player.sendMessage(ChatColor.AQUA + "商店中找不到该物品");
            return;
        }
        double price = PRICES.get(material);
        if (DataManager.getBalance(player.getUniqueId()) < price) {
            player.sendMessage(ChatColor.AQUA + "余额不足，需要 " + price + " 根号币");
            return;
        }
        ItemStack item = new ItemStack(material, 1);
        if (!player.getInventory().addItem(item).isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "背包已满，无法购买");
            return;
        }
        DataManager.addBalance(player.getUniqueId(), -price);
        player.sendMessage(ChatColor.YELLOW + "购买成功！获得 " + ChatColor.BOLD + material.name().toLowerCase() + ChatColor.YELLOW + "，花费 " + price + " 根号币");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("buy");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("buy")) {
            return PRICES.keySet().stream().map(m -> m.name().toLowerCase()).toList();
        }
        return Collections.emptyList();
    }
}