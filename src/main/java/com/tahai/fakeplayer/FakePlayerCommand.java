package com.tahai.fakeplayer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class FakePlayerCommand implements CommandExecutor, TabCompleter {

    private final FakePlayerManager fakePlayerManager = new FakePlayerManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("fakeplayer.isop")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令!");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.GRAY + "用法: /fakeplayer <名字>");
            return true;
        }

        String name = args[0];

        if (fakePlayerManager.getFakePlayer(name) != null) {
            sender.sendMessage(ChatColor.AQUA + "名为 " + name + " 的假人已存在!");
            return true;
        }

        Entity fakePlayer = fakePlayerManager.spawnFakePlayer(name, player.getLocation());
        if (fakePlayer != null) {
            sender.sendMessage(ChatColor.YELLOW + "已生成假人 " + name);
        } else {
            sender.sendMessage(ChatColor.AQUA + "生成假人失败!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}