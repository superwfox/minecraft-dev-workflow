package com.tahai.onlyshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final DataManager dataManager;

    public AdminCommand(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("shop.admin")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令。");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return true;
        }

        if (args.length == 0) {
            Player player = (Player) sender;
            GUIHolder holder = new GUIHolder().createAdminManage();
            holder.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("clearrecord")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.GRAY + "用法: /shopadmin clearrecord <玩家名>");
                return true;
            }
            String playerName = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (!target.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.AQUA + "玩家 " + playerName + " 不存在或从未玩过。");
                return true;
            }
            String uuid = target.getUniqueId().toString();
            for (String itemId : dataManager.getItemList()) {
                dataManager.setPurchaseCount(uuid, itemId, 0);
            }
            dataManager.save();
            sender.sendMessage(ChatColor.YELLOW + "已清除玩家 " + playerName + " 的购买记录。");
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "可用子命令: clearrecord");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("shop.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("clearrecord".startsWith(args[0].toLowerCase())) {
                completions.add("clearrecord");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("clearrecord")) {
            List<String> players = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    players.add(online.getName());
                }
            }
            return players;
        }

        return Collections.emptyList();
    }
}