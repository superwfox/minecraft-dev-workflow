package com.tahai.infinitewarehouse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("warehouse.reload")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("InfiniteWarehouse");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "无法找到 InfiniteWarehouse 插件。");
            return true;
        }
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.YELLOW + "配置文件已重新加载！");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}