package com.tahai.hh;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class HhCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "此命令只能由玩家使用。");
            return true;
        }
        if (!sender.hasPermission("hh.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令。");
            return true;
        }
        Player player = (Player) sender;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("hh");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未加载。");
            return true;
        }

        RegisteredServiceProvider<SessionManager> rsp = Bukkit.getServicesManager().getRegistration(SessionManager.class);
        if (rsp == null) {
            sender.sendMessage(ChatColor.AQUA + "服务未注册。");
            return true;
        }
        SessionManager sessionManager = rsp.getProvider();
        sessionManager.addPlayer(player.getUniqueId());

        List<String> menuLines = plugin.getConfig().getStringList("menu.lines");
        if (menuLines.isEmpty()) {
            sender.sendMessage(ChatColor.AQUA + "菜单未配置。");
            return true;
        }

        boolean papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        for (String line : menuLines) {
            String message = ChatColor.translateAlternateColorCodes('&', line);
            if (papiEnabled) {
                try {
                    Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                    Method setPlaceholders = papiClass.getMethod("setPlaceholders", Player.class, String.class);
                    message = (String) setPlaceholders.invoke(null, player, message);
                } catch (Exception ignored) {
                }
            }
            player.sendMessage(message);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}