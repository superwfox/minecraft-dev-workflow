package com.tahai.playerscan;

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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ScanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("scan.command.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.AQUA + "用法: /scan <IP/玩家名称> <only|overall>");
            return true;
        }

        String target = args[0].toLowerCase();
        String mode = args[1].toLowerCase();

        if (!mode.equals("only") && !mode.equals("overall")) {
            sender.sendMessage(ChatColor.AQUA + "用法: /scan <IP/玩家名称> <only|overall>");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerScan");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "PlayerScan 插件未加载");
            return true;
        }

        IPHistoryStorage storage = new IPHistoryStorage(plugin);
        Map<String, PlayerRecord> records = storage.getRecords();

        List<String> matchedPlayers = new ArrayList<>();

        for (PlayerRecord record : records.values()) {
            Set<String> ips = record.getIps();
            if (ips.isEmpty()) continue;

            if (mode.equals("only")) {
                String lastIp = null;
                for (String ip : ips) {
                    lastIp = ip;
                }
                if (lastIp != null && lastIp.equalsIgnoreCase(target)) {
                    matchedPlayers.add(record.getPlayerName());
                }
            } else {
                for (String ip : ips) {
                    if (ip.equalsIgnoreCase(target)) {
                        matchedPlayers.add(record.getPlayerName());
                        break;
                    }
                }
            }
        }

        if (matchedPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.AQUA + "没有找到匹配的账号");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "匹配到 " + matchedPlayers.size() + " 个账号: " + String.join(", ", matchedPlayers));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("scan.command.use")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            List<String> candidates = new ArrayList<>();
            if ("only".startsWith(prefix)) {
                candidates.add("only");
            }
            if ("overall".startsWith(prefix)) {
                candidates.add("overall");
            }
            return candidates;
        }

        return Collections.emptyList();
    }
}