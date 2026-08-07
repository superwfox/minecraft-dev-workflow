package com.tahai.playerscanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ScanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("scan.admin")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "用法: /scan <IP或玩家名> <only|overall>");
            return true;
        }

        boolean onlyRecent;
        if (args[1].equalsIgnoreCase("only")) {
            onlyRecent = true;
        } else if (args[1].equalsIgnoreCase("overall")) {
            onlyRecent = false;
        } else {
            sender.sendMessage(ChatColor.GRAY + "第二个参数必须是 only 或 overall。");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerScanner");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "PlayerScanner 插件未加载。");
            return true;
        }

        DataManager dataManager = new DataManager(plugin);
        String target = args[0];
        boolean isIp = target.contains(".") || target.contains(":");
        List<String> accounts = isIp
                ? dataManager.getAccountsByIp(target, onlyRecent)
                : dataManager.getAccountsByPlayer(target, onlyRecent);

        if (accounts == null || accounts.isEmpty()) {
            sender.sendMessage(ChatColor.AQUA + "未找到相关玩家。");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "查询结果 (" + accounts.size() + "):");
        for (String name : accounts) {
            sender.sendMessage(" - " + name);
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
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    names.add(player.getName());
                }
            }
            return names;
        }
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            List<String> modes = new ArrayList<>();
            for (String mode : new String[]{"only", "overall"}) {
                if (mode.startsWith(prefix)) {
                    modes.add(mode);
                }
            }
            return modes;
        }
        return Collections.emptyList();
    }
}