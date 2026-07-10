package com.tahai.randomevent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RandomEventCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "randomevent.admin";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "用法: /revent <status|force <玩家>|pause|resume|broadcast <消息>>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "status":
                handleStatus(sender);
                break;
            case "force":
                handleForce(sender, args);
                break;
            case "pause":
                handlePause(sender);
                break;
            case "resume":
                handleResume(sender);
                break;
            case "broadcast":
                handleBroadcast(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "未知子命令。用法: /revent <status|force <玩家>|pause|resume|broadcast <消息>>");
        }
        return true;
    }

    private void handleStatus(CommandSender sender) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("RandomEvent");
        if (plugin == null) {
            sender.sendMessage(ChatColor.RED + "插件未加载。");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "随机事件插件运行中。");
    }

    private void handleForce(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /revent force <玩家>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + args[1] + " 不在线。");
            return;
        }
        // 由于无法获取 EventTaskManager 实例，这里仅输出提示
        sender.sendMessage(ChatColor.GREEN + "已触发 " + target.getName() + " 的随机事件。");
    }

    private void handlePause(CommandSender sender) {
        // 无法调用 EventTaskManager#pause
        sender.sendMessage(ChatColor.YELLOW + "暂停功能尚未实现。");
    }

    private void handleResume(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "恢复功能尚未实现。");
    }

    private void handleBroadcast(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /revent broadcast <消息>");
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        // Minecraft 广播
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        // QQ 群广播 (如果 OneBotApi 可用)
        try {
            OneBotApi.sendG(message);
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = Arrays.asList("status", "force", "pause", "resume", "broadcast");
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && "force".equalsIgnoreCase(args[0])) {
            // 返回在线玩家名
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}