package com.tahai.onlyshop;

import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "只有玩家可以执行此命令");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("shop.buy")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限使用此命令");
            return true;
        }
        new GUIHolder().createPlayerShop().open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}