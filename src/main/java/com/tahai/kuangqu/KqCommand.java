package com.tahai.kuangqu;

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

public class KqCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private static MineManager mineManager;

    public static void setMineManager(MineManager manager) {
        mineManager = manager;
    }

    public KqCommand() {
        this.plugin = Bukkit.getPluginManager().getPlugin("Kuangqu");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (mineManager == null) {
            sender.sendMessage(ChatColor.AQUA + "矿区管理器尚未初始化，请联系管理员。");
            return true;
        }

        if (!sender.hasPermission("kuangqu.admin")) {
            sender.sendMessage(plugin.getConfig().getString("messages.no-permission", ChatColor.AQUA + "你没有权限执行此命令。"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getConfig().getString("messages.usage",
                    ChatColor.GRAY + "用法: /kq <create|reset|remove|list> [参数...]"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                return handleCreate(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender);
            default:
                sender.sendMessage(plugin.getConfig().getString("messages.usage",
                        ChatColor.GRAY + "未知子命令。用法: /kq <create|reset|remove|list>"));
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 10) {
            sender.sendMessage(plugin.getConfig().getString("messages.create-usage",
                    ChatColor.GRAY + "用法: /kq create <名称> <世界> <minX> <minY> <minZ> <maxX> <maxY> <maxZ> <重置时间>"));
            return true;
        }
        String name = args[1];
        String world = args[2];
        int minX, minY, minZ, maxX, maxY, maxZ;
        try {
            minX = Integer.parseInt(args[3]);
            minY = Integer.parseInt(args[4]);
            minZ = Integer.parseInt(args[5]);
            maxX = Integer.parseInt(args[6]);
            maxY = Integer.parseInt(args[7]);
            maxZ = Integer.parseInt(args[8]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfig().getString("messages.invalid-coordinate",
                    ChatColor.AQUA + "坐标必须是整数。"));
            return true;
        }
        String resetTime = args[9];

        if (mineManager.getMineByName(name) != null) {
            sender.sendMessage(plugin.getConfig().getString("messages.create-exists",
                    ChatColor.AQUA + "矿区 " + name + " 已存在。"));
            return true;
        }

        MineManager.Mine mine = mineManager.addMine(name, world, minX, minY, minZ, maxX, maxY, maxZ, resetTime);
        if (mine == null) {
            sender.sendMessage(plugin.getConfig().getString("messages.create-fail",
                    ChatColor.AQUA + "创建矿区失败。"));
            return true;
        }
        mineManager.resetMine(mine);
        String msg = plugin.getConfig().getString("messages.create-success",
                ChatColor.YELLOW + "矿区 " + name + " 已创建并重置。");
        sender.sendMessage(msg.replace("{name}", name).replace("{world}", world)
                .replace("{coords}", minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ));
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfig().getString("messages.reset-usage",
                    ChatColor.GRAY + "用法: /kq reset <矿区名称>"));
            return true;
        }
        String name = args[1];
        MineManager.Mine mine = mineManager.getMineByName(name);
        if (mine == null) {
            sender.sendMessage(plugin.getConfig().getString("messages.not-found",
                    ChatColor.AQUA + "未找到矿区 " + name + "。"));
            return true;
        }
        mineManager.resetMine(mine);
        sender.sendMessage(plugin.getConfig().getString("messages.reset-success",
                ChatColor.YELLOW + "矿区 " + name + " 已重置。").replace("{name}", name));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfig().getString("messages.remove-usage",
                    ChatColor.GRAY + "用法: /kq remove <矿区名称>"));
            return true;
        }
        String name = args[1];
        if (!mineManager.removeMine(name)) {
            sender.sendMessage(plugin.getConfig().getString("messages.not-found",
                    ChatColor.AQUA + "未找到矿区 " + name + "。"));
            return true;
        }
        sender.sendMessage(plugin.getConfig().getString("messages.remove-success",
                ChatColor.YELLOW + "矿区 " + name + " 已删除。").replace("{name}", name));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        List<MineManager.Mine> mines = mineManager.getAllMines();
        if (mines.isEmpty()) {
            sender.sendMessage(plugin.getConfig().getString("messages.list-empty",
                    ChatColor.GRAY + "当前没有矿区。"));
            return true;
        }
        String header = plugin.getConfig().getString("messages.list-header",
                ChatColor.GRAY + "矿区列表:");
        sender.sendMessage(header);
        for (MineManager.Mine mine : mines) {
            String entry = plugin.getConfig().getString("messages.list-entry",
                    ChatColor.GRAY + " - {name} ({world}, {coords}) 重置时间: {resetTime}");
            String coords = mine.getMinX() + "," + mine.getMinY() + "," + mine.getMinZ()
                    + " -> " + mine.getMaxX() + "," + mine.getMaxY() + "," + mine.getMaxZ();
            String formatted = entry.replace("{name}", mine.getName())
                    .replace("{world}", mine.getWorldName())
                    .replace("{coords}", coords)
                    .replace("{resetTime}", mine.getResetTime());
            sender.sendMessage(formatted);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("kuangqu.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("create");
            subs.add("reset");
            subs.add("remove");
            subs.add("list");
            return subs;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("reset") || sub.equals("remove")) {
                if (mineManager == null) {
                    return Collections.emptyList();
                }
                List<String> names = new ArrayList<>();
                for (MineManager.Mine mine : mineManager.getAllMines()) {
                    names.add(mine.getName());
                }
                return names;
            }
        }

        return Collections.emptyList();
    }
}