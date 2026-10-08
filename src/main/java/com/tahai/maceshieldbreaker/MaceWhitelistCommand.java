package com.tahai.maceshieldbreaker;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaceWhitelistCommand implements CommandExecutor, TabCompleter {

    private WhitelistManager whitelist() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MaceShieldBreaker");
        if (plugin == null) {
            return null;
        }
        try {
            Object manager = plugin.getClass().getMethod("getWhitelistManager").invoke(plugin);
            if (manager instanceof WhitelistManager) {
                return (WhitelistManager) manager;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            return false;
        }

        if (args.length < 1) {
            return false;
        }

        String sub = args[0].toLowerCase();
        WhitelistManager whitelist = whitelist();

        switch (sub) {
            case "add" -> {
                if (args.length < 2 || whitelist == null) {
                    return false;
                }
                whitelist.add(args[1]);
                return true;
            }
            case "remove" -> {
                if (args.length < 2 || whitelist == null) {
                    return false;
                }
                whitelist.remove(args[1]);
                return true;
            }
            case "list" -> {
                if (whitelist == null) {
                    return false;
                }
                List<String> snapshot = whitelist.getNames();
                return snapshot != null;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            for (String option : List.of("add", "remove", "list")) {
                if (option.startsWith(args[0].toLowerCase())) {
                    result.add(option);
                }
            }
            return result;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add") || sub.equals("remove")) {
                List<String> result = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        result.add(player.getName());
                    }
                }
                return result;
            }
        }

        return Collections.emptyList();
    }
}