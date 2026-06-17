package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ShenpanCommand implements CommandExecutor, TabCompleter {

    private final VoteManager voteManager;

    public ShenpanCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "用法: /shenpan <start|vote>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "用法: /shenpan start <玩家1> <玩家2>");
                    return true;
                }
                Player target1 = Bukkit.getPlayerExact(args[1]);
                Player target2 = Bukkit.getPlayerExact(args[2]);
                if (target1 == null || target2 == null) {
                    sender.sendMessage(ChatColor.RED + "玩家不在线或不存在");
                    return true;
                }
                if (voteManager.startVote(target1.getUniqueId(), target2.getUniqueId())) {
                    sender.sendMessage(ChatColor.GREEN + "投票已成功开始");
                } else {
                    sender.sendMessage(ChatColor.RED + "无法开始投票（可能已有投票正在进行）");
                }
                return true;
            case "vote":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /shenpan vote <目标>");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "只有玩家才能投票");
                    return true;
                }
                Player voter = (Player) sender;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "目标玩家不在线或不存在");
                    return true;
                }
                if (voteManager.vote(voter.getUniqueId(), target.getUniqueId())) {
                    sender.sendMessage(ChatColor.GREEN + "投票成功");
                } else {
                    sender.sendMessage(ChatColor.RED + "投票失败（可能没有活跃的投票或已投过票）");
                }
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "未知子命令，可用: start, vote");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("start");
            subs.add("vote");
            return subs;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("vote")) {
                return getOnlinePlayerNames();
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
            return getOnlinePlayerNames();
        }

        return Collections.emptyList();
    }

    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
        }
        return names;
    }
}