package com.tahai.anvilplus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaxEnchantCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("anvilplus.maxenchant")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令。");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.AQUA + "用法: /maxenchant <附魔ID> <等级>");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.AQUA + "请在主手持有物品。");
            return true;
        }

        String enchantKeyStr = args[0].toLowerCase();
        NamespacedKey key = NamespacedKey.fromString(enchantKeyStr);
        if (key == null && !enchantKeyStr.contains(":")) {
            key = NamespacedKey.minecraft(enchantKeyStr);
        }
        if (key == null) {
            player.sendMessage(ChatColor.AQUA + "无效的附魔ID: " + args[0]);
            return true;
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(key);
        if (enchantment == null) {
            player.sendMessage(ChatColor.AQUA + "找不到附魔: " + args[0]);
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.AQUA + "等级必须是整数: " + args[1]);
            return true;
        }
        if (level <= 0) {
            player.sendMessage(ChatColor.AQUA + "等级必须大于0。");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.hasEnchant(enchantment)) {
            meta.removeEnchant(enchantment);
        }
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);

        player.sendMessage(ChatColor.YELLOW + "已为主手物品应用附魔 " + enchantment.getKey() + " " + level);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("anvilplus.maxenchant")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (Enchantment ench : Registry.ENCHANTMENT) {
                String key = ench.getKey().toString();
                if (key.startsWith(prefix)) {
                    completions.add(key);
                }
            }
            Collections.sort(completions);
            return completions;
        }
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (String s : new String[]{"1", "5", "10", "100"}) {
                if (s.startsWith(prefix)) {
                    suggestions.add(s);
                }
            }
            return suggestions;
        }
        return Collections.emptyList();
    }
}