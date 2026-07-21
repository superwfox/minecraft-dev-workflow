package com.tahai.qinglong.command;

import com.tahai.qinglong.gui.MenuGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class QinglongCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "此命令只能由玩家执行");
            return true;
        }

        if (!sender.hasPermission("qinglong.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || (args.length >= 1 && args[0].equalsIgnoreCase("menu"))) {
            // 直接打开主菜单
            new MenuGUI().openMainMenu(player);
            return true;
        }

        // 未知参数
        sender.sendMessage(ChatColor.AQUA + "用法: /qinglong [menu]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("menu");
        }
        return Collections.emptyList();
    }
}