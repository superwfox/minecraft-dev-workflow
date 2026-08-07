package com.tahai.scancheck;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ScanCommand implements CommandExecutor, TabCompleter {

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)$"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("scan.admin")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "用法: /scan <目标> <模式>");
            return true;
        }

        String target = args[0];
        String mode = args[1].toLowerCase();
        if (!mode.equals("only") && !mode.equals("overall")) {
            sender.sendMessage(ChatColor.AQUA + "模式必须是 only 或 overall。");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("ScanCheck");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未加载。");
            return true;
        }

        DataManager dataManager = new DataManager(plugin);

        String ip;
        if (isIp(target)) {
            ip = target;
        } else {
            ip = dataManager.getLastIp(target);
            if (ip == null || ip.isEmpty()) {
                sender.sendMessage(ChatColor.AQUA + "未找到玩家 " + target + " 的记录。");
                return true;
            }
        }

        List<String> players;
        if (mode.equals("only")) {
            players = dataManager.findPlayersByLastIp(ip);
        } else {
            players = dataManager.findPlayersByIpHistory(ip);
        }

        if (players == null || players.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "没有玩家匹配 IP " + ip + "。");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "匹配玩家(" + players.size() + "): " + String.join(", ", players));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("scan.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(prefix)) {
                    suggestions.add(online.getName());
                }
            }
            return suggestions;
        }
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            List<String> modes = new ArrayList<>();
            if ("only".startsWith(prefix)) modes.add("only");
            if ("overall".startsWith(prefix)) modes.add("overall");
            return modes;
        }
        return Collections.emptyList();
    }

    private boolean isIp(String input) {
        return IP_PATTERN.matcher(input).matches();
    }
}