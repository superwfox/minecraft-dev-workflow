package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VoteCommand implements CommandExecutor, TabCompleter {

    private final VoteManager voteManager;

    public VoteCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能执行此命令");
            return true;
        }

        Player player = (Player) sender;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Shenpan");
        if (plugin == null) {
            return true;
        }

        String prefix = plugin.getConfig().getString("messages.prefix", "§6[审判] ");

        if (args.length != 1) {
            String usage = plugin.getConfig().getString("messages.invalid-usage", "用法: /vote <玩家>");
            player.sendMessage(prefix + usage);
            return true;
        }

        if (!voteManager.isVoting()) {
            player.sendMessage(prefix + "当前没有进行中的投票");
            return true;
        }

        String candidate = args[0];
        boolean success = voteManager.vote(player, candidate);

        if (success) {
            String successMsg = plugin.getConfig().getString("messages.vote-success", "投票成功");
            player.sendMessage(prefix + successMsg);
        } else {
            player.sendMessage(prefix + "投票失败，请确认候选人名称正确");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}