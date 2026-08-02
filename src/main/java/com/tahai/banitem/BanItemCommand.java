package com.tahai.banitem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BanItemCommand implements CommandExecutor, TabCompleter {

    private final DatabaseManager databaseManager;

    public BanItemCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.YELLOW + "该命令只能由玩家执行");
            return true;
        }

        if (!sender.hasPermission("banitem.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /banitem <add|remove|list>");
            return true;
        }

        Player player = (Player) sender;
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add":
                ItemStack handAdd = player.getInventory().getItemInMainHand();
                if (handAdd.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.AQUA + "请手持物品");
                    return true;
                }
                if (databaseManager.addBanned(handAdd)) {
                    player.sendMessage(ChatColor.YELLOW + "物品已禁用");
                } else {
                    player.sendMessage(ChatColor.AQUA + "该物品已经在禁用列表中");
                }
                return true;

            case "remove":
                ItemStack handRemove = player.getInventory().getItemInMainHand();
                if (handRemove.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.AQUA + "请手持物品");
                    return true;
                }
                if (databaseManager.removeBanned(handRemove)) {
                    player.sendMessage(ChatColor.YELLOW + "物品已解除禁用");
                } else {
                    player.sendMessage(ChatColor.AQUA + "该物品不在禁用列表中");
                }
                return true;

            case "list":
                Set<String> bannedItems = databaseManager.getBannedItems();
                if (bannedItems == null || bannedItems.isEmpty()) {
                    player.sendMessage(ChatColor.AQUA + "当前没有禁用的物品");
                    return true;
                }
                player.sendMessage(ChatColor.YELLOW + "已禁用的物品:");
                for (String data : bannedItems) {
                    ItemStack item = databaseManager.deserialize(data);
                    if (item == null) {
                        continue;
                    }
                    String displayName = null;
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        displayName = item.getItemMeta().getDisplayName();
                    }
                    if (displayName == null) {
                        player.sendMessage(ChatColor.YELLOW + "- " + item.getType().name());
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "- " + item.getType().name() + " (" + displayName + ")");
                    }
                }
                return true;

            default:
                sender.sendMessage(ChatColor.YELLOW + "未知子命令，用法: /banitem <add|remove|list>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            if ("add".startsWith(input)) {
                completions.add("add");
            }
            if ("remove".startsWith(input)) {
                completions.add("remove");
            }
            if ("list".startsWith(input)) {
                completions.add("list");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}