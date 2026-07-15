package com.tahai.voteskipnight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class VoteCommand implements CommandExecutor, TabCompleter {

    private static VoteManager currentVote = null;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "只有玩家可以执行此命令。");
            return true;
        }

        if (!sender.hasPermission("voteskipnight.tgzs")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            // 发起投票
            World world = player.getWorld();
            long time = world.getTime() % 24000;
            if (time < 13000 || time >= 23000) {
                player.sendMessage(ChatColor.AQUA + "现在不是夜晚，无法发起跳过夜晚投票。");
                return true;
            }

            if (currentVote != null) {
                player.sendMessage(ChatColor.AQUA + "已有投票正在进行，无法发起新投票。");
                return true;
            }

            int currentDay = (int) (world.getFullTime() / 24000);
            VoteManager vote = new VoteManager();
            boolean initiated = vote.initiateVote(uuid, currentDay);
            if (!initiated) {
                player.sendMessage(ChatColor.AQUA + "发起投票失败。");
                return true;
            }
            currentVote = vote;

            // 自动同意
            vote.addVote(uuid, true);

            // 广播
            Bukkit.broadcastMessage(ChatColor.YELLOW + "[投票] " + ChatColor.GRAY + "玩家 " + ChatColor.WHITE
                    + player.getName() + ChatColor.GRAY + " 发起跳过夜晚投票！输入 "
                    + ChatColor.WHITE + "/tgzs yes" + ChatColor.GRAY + " 同意，"
                    + ChatColor.WHITE + "/tgzs no" + ChatColor.GRAY + " 反对。"
                    + ChatColor.YELLOW + "（60秒内）");

            // 启动超时任务
            Plugin plugin = Bukkit.getPluginManager().getPlugin("VoteSkipNight");
            if (plugin != null) {
                new VoteTimeoutTask().runTaskLater(plugin, 1200L);
            }
            return true;
        }

        if (args.length == 1 && (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("no"))) {
            if (currentVote == null) {
                player.sendMessage(ChatColor.AQUA + "当前没有进行中的投票。");
                return true;
            }

            boolean agree = args[0].equalsIgnoreCase("yes");
            boolean success = currentVote.addVote(uuid, agree);
            if (success) {
                player.sendMessage(ChatColor.YELLOW + "你已成功投票 " + (agree ? "同意" : "反对") + "。");
            } else {
                player.sendMessage(ChatColor.AQUA + "投票失败，你可能已经投过票了。");
            }
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "用法: /tgzs (空) 发起投票，或 /tgzs <yes/no> 投票");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Arrays.asList("yes", "no").stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}