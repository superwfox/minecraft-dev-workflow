package com.tahai.pvpduel;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class VSCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("vs.pvp")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限执行此命令");
            return true;
        }
        new DuelGUI(player, ChatColor.YELLOW + "选择决斗对手").open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}