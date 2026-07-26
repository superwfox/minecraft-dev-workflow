package com.tahai.hh;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HhCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "只有玩家才能使用此命令");
            return true;
        }

        if (!sender.hasPermission("hh.open")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令");
            return true;
        }

        if (args.length == 0) {
            Player player = (Player) sender;
            GuiHolder holder = new GuiHolder();
            holder.open(player);
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "使用方法: /" + label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}