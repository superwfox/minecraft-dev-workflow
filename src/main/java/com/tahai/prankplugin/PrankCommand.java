package com.tahai.prankplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PrankCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令。");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("prank.op")) {
            player.sendMessage("§c你没有权限执行此命令。");
            return true;
        }

        PrankGui gui;
        try {
            gui = PrankGui.class.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            player.sendMessage("§c无法打开恶搞菜单。");
            return true;
        }

        player.openInventory(gui.createGui());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}