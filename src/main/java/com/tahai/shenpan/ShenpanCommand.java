package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShenpanCommand implements CommandExecutor, TabCompleter {

    private final VoteManager voteManager;

    public ShenpanCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("shenpan.use")) {
            sender.sendMessage(Messages.getInstance().getString("no-permission"));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Messages.getInstance().getString("player-only"));
                return true;
            }
            if (voteManager.isActive()) {
                sender.sendMessage(Messages.getInstance().getString("vote-start"));
                return true;
            }
            voteManager.startVote((Player) sender, 60);
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            if (!voteManager.isActive()) {
                sender.sendMessage(Messages.getInstance().getString("vote-end"));
                return true;
            }
            voteManager.cancelVote();
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.getInstance().getString("player-only"));
            return true;
        }
        if (voteManager.isActive()) {
            sender.sendMessage(Messages.getInstance().getString("vote-start"));
            return true;
        }
        voteManager.startVote((Player) sender, 60);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("cancel");
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}