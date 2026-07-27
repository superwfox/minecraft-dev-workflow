package com.tahai.hs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class HSCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "只有玩家可以使用此命令。");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("hs.use")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限使用此命令。");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("HS");
        if (plugin == null) return true;

        new RecycleGui(player, plugin);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}