package com.tahai.itemban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItembanCommand implements CommandExecutor, TabCompleter {

    private final DataManager dataManager;

    public ItembanCommand() {
        this.dataManager = new DataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemBan");
        if (plugin == null) {
            return true;
        }

        if (!sender.hasPermission("custom.itemban")) {
            sender.sendMessage(colorize(plugin.getConfig().getString(
                    "messages.no-permission",
                    ChatColor.AQUA + "你没有权限执行此命令。")));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, plugin);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                return handleAdd(sender, args, plugin);
            case "remove":
                return handleRemove(sender, args, plugin);
            case "list":
                return handleList(sender, plugin);
            case "reload":
                return handleReload(sender, plugin);
            default:
                sendUsage(sender, plugin);
                return true;
        }
    }

    private boolean handleAdd(CommandSender sender, String[] args, Plugin plugin) {
        if (args.length < 2) {
            sender.sendMessage(colorize(plugin.getConfig().getString(
                    "messages.add-usage",
                    ChatColor.YELLOW + "用法: /itemban add <key>")));
            return true;
        }

        String key = args[1];
        if (dataManager.add(key)) {
            dataManager.save();
            sender.sendMessage(colorize(plugin.getConfig().getString(
                    "messages.add-success",
                    ChatColor.YELLOW + "已添加 NBT 键: " + key)));
        } else {
            sender.sendMessage(colorize(plugin.getConfig().getString(
                    "messages.add-already",
                    ChatColor.AQUA + "该 NBT 键已存在: " + key)));
        }
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args, Plugin plugin) {
        if (args.length < 2) {
            sender.sendMessage(colorize(plugin.getConfig().getString(
                    "messages.remove-usage",
                    ChatColor.YELLOW + "用法: /itemban remove <key>")));
            return true;
        }

        String key = args[1];
        if (dataManager.remove(key)) {
            dataManager.save();
            sender.sendMessage(colorize(plugin.getConfig().getString(
                    "messages.remove-success",
                    ChatColor.YELLOW + "已移除 NBT 键: " + key)));
        } else {
            sender.sendMessage(colorize(plugin.getConfig().getString(
                    "messages.remove-not-found",
                    ChatColor.AQUA + "该 NBT 键不存在: " + key)));
        }
        return true;
    }

    private boolean handleList(CommandSender sender, Plugin plugin) {
        List<String> keys = dataManager.list();
        if (keys.isEmpty()) {
            sender.sendMessage(colorize(plugin.getConfig().getString(
                    "messages.list-empty",
                    ChatColor.GRAY + "当前没有封禁的 NBT 键。")));
        } else {
            String header = plugin.getConfig().getString(
                    "messages.list-header",
                    ChatColor.GRAY + "被封禁的 NBT 键 (%size%):");
            sender.sendMessage(colorize(header.replace("%size%", String.valueOf(keys.size()))));

            for (String key : keys) {
                String entry = plugin.getConfig().getString(
                        "messages.list-entry",
                        ChatColor.GRAY + " - %key%");
                sender.sendMessage(colorize(entry.replace("%key%", key)));
            }
        }
        return true;
    }

    private boolean handleReload(CommandSender sender, Plugin plugin) {
        plugin.reloadConfig();
        dataManager.reload();
        sender.sendMessage(colorize(plugin.getConfig().getString(
                "messages.reload-success",
                ChatColor.YELLOW + "配置与数据已重载。")));
        return true;
    }

    private void sendUsage(CommandSender sender, Plugin plugin) {
        sender.sendMessage(colorize(plugin.getConfig().getString(
                "messages.usage",
                ChatColor.GRAY + "用法: /itemban <add|remove|list|reload>")));
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("custom.itemban")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("add");
            subs.add("remove");
            subs.add("list");
            subs.add("reload");
            return filter(subs, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                return filter(dataManager.list(), args[1]);
            }
            if (args[0].equalsIgnoreCase("add")) {
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> candidates, String prefix) {
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(candidate);
            }
        }
        return result;
    }
}