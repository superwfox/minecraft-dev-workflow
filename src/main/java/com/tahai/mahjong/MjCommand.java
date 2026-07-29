package com.tahai.mahjong;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MjCommand implements CommandExecutor, TabCompleter {

    private final GameManager gameManager;

    public MjCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mjadmin.create")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /mj create [mode] - 创建一桌麻将");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.AQUA + "只有玩家可以创建麻将桌。");
                    return true;
                }
                Player player = (Player) sender;
                GameMode mode = GameMode.FOUR_PLAYER;
                if (args.length > 1) {
                    try {
                        mode = GameMode.valueOf(args[1].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.AQUA + "无效的模式: " + args[1] + "。可用的模式: FOUR_PLAYER, THREE_PLAYER");
                        return true;
                    }
                }
                String tableId = gameManager.createTable(player, mode);
                sender.sendMessage(ChatColor.YELLOW + "成功创建麻将桌 " + ChatColor.BOLD + tableId + ChatColor.YELLOW + "。");
                break;
            default:
                sender.sendMessage(ChatColor.AQUA + "未知子命令: " + sub);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("mjadmin.create")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("create");
            return filter(subs, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            List<String> modes = new ArrayList<>();
            for (GameMode mode : GameMode.values()) {
                modes.add(mode.name().toLowerCase());
            }
            return filter(modes, args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}