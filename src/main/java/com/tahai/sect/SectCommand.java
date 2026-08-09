package com.tahai.sect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class SectCommand implements CommandExecutor, TabCompleter {

    private final ClanManager clanManager;
    private final ClanGUI gui;

    public SectCommand(ClanManager clanManager, ClanGUI gui) {
        this.clanManager = clanManager;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("zongmen.sect")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行该命令。");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "用法: /sect create <宗门名称> | gui | war <宗门> | accept");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return true;
        }
        Player player = (Player) sender;
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.GRAY + "用法: /sect create <宗门名称>");
                    return true;
                }
                if (clanManager.createClan(player, args[1])) {
                    player.sendMessage(ChatColor.YELLOW + "宗门 " + ChatColor.BOLD + args[1] + ChatColor.YELLOW + " 创建成功！");
                } else {
                    player.sendMessage(ChatColor.AQUA + "宗门创建失败，请检查名称是否重复、是否已选择领地或金币是否充足。");
                }
                return true;
            case "gui":
                gui.open(player);
                return true;
            case "war":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.GRAY + "用法: /sect war <宗门名称>");
                    return true;
                }
                if (clanManager.inviteWar(player, args[1])) {
                    player.sendMessage(ChatColor.YELLOW + "已向宗门 " + ChatColor.BOLD + args[1] + ChatColor.YELLOW + " 发起宗门战邀请！");
                } else {
                    player.sendMessage(ChatColor.AQUA + "宗门战邀请失败，请检查目标宗门是否存在或当前宗门战状态。");
                }
                return true;
            case "accept":
                if (clanManager.acceptWar(player)) {
                    player.sendMessage(ChatColor.YELLOW + "已成功接受宗门战邀请！");
                } else {
                    player.sendMessage(ChatColor.AQUA + "没有可接受的宗门战邀请。");
                }
                return true;
            default:
                player.sendMessage(ChatColor.GRAY + "未知子命令。用法: /sect create <宗门名称> | gui | war <宗门> | accept");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("zongmen.sect")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> candidates = Arrays.asList("create", "gui", "war", "accept");
            if (prefix.isEmpty()) {
                return candidates;
            }
            List<String> result = new ArrayList<>();
            for (String candidate : candidates) {
                if (candidate.startsWith(prefix)) {
                    result.add(candidate);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}