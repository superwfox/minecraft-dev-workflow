package com.tahai.customhud;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomHUDCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customhud.admin")) {
            sender.sendMessage(ChatColor.GRAY + "你没有权限执行此命令。");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.GRAY + "用法: /" + label + " reload");
            return true;
        }

        if (!args[0].equalsIgnoreCase("reload")) {
            return false;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CustomHUD");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未加载。");
            return true;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            HUDManager hudManager = new HUDManager(plugin);
            hudManager.reload();
            hudManager.updateAllPlayers();
        });

        sender.sendMessage(ChatColor.YELLOW + "自定义 HUD 已" + ChatColor.BOLD + "重新加载" + ChatColor.YELLOW + "。");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customhud.admin")) {
            return Collections.emptyList();
        }

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