package com.tahai.fakeplayerplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FakePlayerCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("fakeplayer.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令。");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.GRAY + "用法: /fakeplayer <名字>");
            return true;
        }

        String name = args[0];
        Player player = (Player) sender;
        Location location = player.getLocation();

        FakePlayerManager manager = new FakePlayerManager();
        boolean success = manager.createFakePlayer(name, location);
        if (success) {
            sender.sendMessage(ChatColor.YELLOW + "假人 " + name + " 已创建。");
        } else {
            sender.sendMessage(ChatColor.AQUA + "假人 " + name + " 创建失败，可能名字已存在。");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(online.getName());
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}