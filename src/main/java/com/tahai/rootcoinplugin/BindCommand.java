package com.tahai.rootcoinplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class BindCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GRAY + "该命令只能由玩家执行");
            return true;
        }
        if (!sender.hasPermission("rootcoin.bind")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令");
            return true;
        }
        Player player = (Player) sender;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("RootCoinPlugin");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未加载");
            return true;
        }
        DataManager data = null;
        try {
            Method getDataManager = plugin.getClass().getMethod("getDataManager");
            data = (DataManager) getDataManager.invoke(plugin);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.AQUA + "无法获取数据管理器");
            return true;
        }
        if (data == null) {
            sender.sendMessage(ChatColor.AQUA + "无法获取数据管理器");
            return true;
        }
        if (data.getQQ(player.getUniqueId()) != null) {
            sender.sendMessage(ChatColor.AQUA + "你已完成绑定，请勿重复绑定");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.GRAY + "用法: /bind <QQ号>");
            return true;
        }
        String qq = args[0];
        if (!qq.matches("\\d{5,12}")) {
            sender.sendMessage(ChatColor.AQUA + "QQ号格式不正确");
            return true;
        }
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
        if (data.getQQCountForIP(ip) >= 5) {
            sender.sendMessage(ChatColor.AQUA + "同一IP最多绑定5个QQ号");
            return true;
        }
        sender.sendMessage(ChatColor.GRAY + "请使用QQ扫描下方二维码完成验证...");
        sender.sendMessage(ChatColor.GRAY + "[模拟二维码] https://example.com/qr?qq=" + qq + "&ip=" + ip);
        sender.sendMessage(ChatColor.GRAY + "验证已通过，正在写入绑定信息...");
        if (data.bindPlayer(player.getUniqueId(), qq, ip)) {
            sender.sendMessage(ChatColor.YELLOW + "绑定成功，QQ: " + qq);
        } else {
            sender.sendMessage(ChatColor.AQUA + "绑定失败，请稍后重试");
        }
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