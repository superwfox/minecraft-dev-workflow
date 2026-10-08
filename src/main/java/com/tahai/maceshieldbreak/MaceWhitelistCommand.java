package com.tahai.maceshieldbreak;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaceWhitelistCommand implements CommandExecutor, TabCompleter {

    private final MaceWhitelistManager manager;

    public MaceWhitelistCommand() {
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("MaceShieldBreak");
        this.manager = new MaceWhitelistManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("maceshieldbreak.command") || !sender.isOp()) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行该命令。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "用法：/" + label + " add|remove <玩家>");
            return true;
        }
        String sub = args[0].toLowerCase();
        String target = args[1];
        if (sub.equals("add")) {
            manager.add(target);
            sender.sendMessage(ChatColor.YELLOW + "已加入白名单：" + ChatColor.BOLD + target);
        } else if (sub.equals("remove")) {
            manager.remove(target);
            sender.sendMessage(ChatColor.YELLOW + "已移出白名单：" + ChatColor.BOLD + target);
        } else {
            sender.sendMessage(ChatColor.GRAY + "用法：/" + label + " add|remove <玩家>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            String prefix = args[0].toLowerCase();
            for (String s : new String[]{"add", "remove"}) {
                if (s.startsWith(prefix)) {
                    out.add(s);
                }
            }
            return out;
        }
        if (args.length == 2) {
            List<String> out = new ArrayList<>();
            String prefix = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) {
                    out.add(p.getName());
                }
            }
            return out;
        }
        return Collections.emptyList();
    }
}