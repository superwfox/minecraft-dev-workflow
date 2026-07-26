package com.tahai.hh;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class OpenMenuCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hh.command.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行");
            return true;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("hh");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未正确加载");
            return true;
        }
        ExchangeMenuGUI gui = new ExchangeMenuGUI(plugin);
        Inventory inv = gui.create(plugin);
        Player player = (Player) sender;
        player.openInventory(inv);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}