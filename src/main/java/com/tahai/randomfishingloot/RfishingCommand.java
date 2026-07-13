package com.tahai.randomfishingloot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RfishingCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "用法: /rfishing <reload|give [player]>");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("RandomFishingLoot");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未正确加载。");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("randomfishing.reload")) {
                sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令！");
                return true;
            }
            ConfigManager configManager = new ConfigManager(plugin);
            configManager.reloadConfig();
            sender.sendMessage(ChatColor.YELLOW + "配置文件已重载。");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("randomfishing.give")) {
                sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令！");
                return true;
            }

            Player target;
            if (args.length >= 2) {
                target = Bukkit.getPlayerExact(args[1]);
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(ChatColor.AQUA + "请指定一个玩家。");
                return true;
            }

            if (target == null || !target.isOnline()) {
                sender.sendMessage(ChatColor.AQUA + "玩家不在线或不存在。");
                return true;
            }

            ConfigManager configManager = new ConfigManager(plugin);
            LootManager lootManager = new LootManager(configManager);
            ItemStack loot = lootManager.generateRandomLoot();
            target.getInventory().addItem(loot);
            sender.sendMessage(ChatColor.YELLOW + "已将随机钓鱼装备给予 " + target.getName());
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "未知子命令。用法: /rfishing <reload|give [player]>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (sender.hasPermission("randomfishing.reload")) {
                subcommands.add("reload");
            }
            if (sender.hasPermission("randomfishing.give")) {
                subcommands.add("give");
            }
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("randomfishing.give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}