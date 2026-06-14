package com.example.coppersword;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CopperSwordCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("coppersword.command")) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "你没有权限使用此命令");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("CopperSword");
            if (plugin != null) {
                plugin.reloadConfig();
            }
            ConfigManager configManager = new ConfigManager();
            configManager.reload();
            sender.sendMessage(configManager.getMessage("reload-success"));
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            new CoolDownGui(player).open(player);
        } else {
            sender.sendMessage(org.bukkit.ChatColor.RED + "只有玩家可以使用此命令");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}