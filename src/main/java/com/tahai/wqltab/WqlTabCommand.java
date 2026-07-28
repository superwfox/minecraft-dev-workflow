package com.tahai.wqltab;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class WqlTabCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;
    private final Plugin plugin;
    private final DisplayTask displayTask;

    public WqlTabCommand(ConfigManager configManager, Plugin plugin, DisplayTask displayTask) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.displayTask = displayTask;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            return false;
        }
        try {
            configManager.reloadConfig();
            displayTask.cancel();
            new DisplayTask(configManager).runTaskTimer(plugin, 0L, 20L);
            sender.sendMessage(ChatColor.YELLOW + "配置重载成功。");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.AQUA + "配置重载失败，请检查控制台。");
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}