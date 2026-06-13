package me.iw46dev.itemcleanertitle;

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

public class ItemWardenCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("iw46dev.iw")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家才能执行此命令.");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemCleanerTitle");
        if (plugin == null) return false;

        MainGUIHolder guiHolder = new MainGUIHolder(27, ChatColor.DARK_GRAY + "物品清理主设置");
        ((Player) sender).openInventory(guiHolder.getInventory());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}