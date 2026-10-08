package com.tahai.maceshieldbreak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class MaceCommand implements CommandExecutor, TabCompleter {

    private final WhitelistManager whitelistManager;

    public MaceCommand(WhitelistManager whitelistManager) {
        this.whitelistManager = whitelistManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("whitelist")) {
            sendUsage(sender, label);
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        if (!action.equals("add") && !action.equals("remove")) {
            sendUsage(sender, label);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage(ChatColor.AQUA + "未找到在线玩家: " + args[2]);
            return true;
        }

        if (action.equals("add")) {
            boolean added = whitelistManager.add(target.getUniqueId());
            whitelistManager.save();
            if (added) {
                sender.sendMessage(ChatColor.YELLOW + "已将 " + ChatColor.BOLD + target.getName()
                        + ChatColor.YELLOW + " 加入重锤破盾白名单。");
            } else {
                sender.sendMessage(ChatColor.AQUA + target.getName() + " 已在重锤破盾白名单中。");
            }
        } else {
            boolean removed = whitelistManager.remove(target.getUniqueId());
            whitelistManager.save();
            if (removed) {
                sender.sendMessage(ChatColor.YELLOW + "已将 " + ChatColor.BOLD + target.getName()
                        + ChatColor.YELLOW + " 移出重锤破盾白名单。");
            } else {
                sender.sendMessage(ChatColor.AQUA + target.getName() + " 不在重锤破盾白名单中。");
            }
        }
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GRAY + "用法: /" + label + " whitelist add|remove <玩家>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Collections.singletonList("whitelist"), args[0]);
        }
        if (args.length == 2) {
            List<String> options = new ArrayList<>();
            options.add("add");
            options.add("remove");
            return filter(options, args[1]);
        }
        if (args.length == 3) {
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            return filter(names, args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        String lower = prefix.toLowerCase(Locale.ROOT);
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}