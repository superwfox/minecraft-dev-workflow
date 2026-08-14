package com.tahai.lfcworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LfcWorldCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lfcworld.admin")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "用法: /lfcworld ban <世界名称> <物品ID> | /lfcworld reload");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("LfcWorld");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未加载。");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.YELLOW + "配置已重载。");
            return true;
        }

        if (args[0].equalsIgnoreCase("ban")) {
            if (args.length != 3) {
                sender.sendMessage(ChatColor.GRAY + "用法: /lfcworld ban <世界名称> <物品ID>");
                return true;
            }

            String worldName = args[1];
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(ChatColor.AQUA + "世界 '" + worldName + "' 不存在。");
                return true;
            }

            Material material = Material.matchMaterial(args[2]);
            if (material == null) {
                sender.sendMessage(ChatColor.AQUA + "物品 '" + args[2] + "' 无效。");
                return true;
            }

            FileConfiguration config = plugin.getConfig();
            String path = "world-bans." + worldName;
            List<String> bannedItems = config.getStringList(path);
            String itemId = material.name();
            if (!bannedItems.contains(itemId)) {
                bannedItems.add(itemId);
            }
            config.set(path, bannedItems);
            plugin.saveConfig();

            sender.sendMessage(ChatColor.YELLOW + "已添加物品 " + itemId + " 到世界 " + worldName + " 的禁用列表。");
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "用法: /lfcworld ban <世界名称> <物品ID> | /lfcworld reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("ban", "reload");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("ban")) {
            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("ban")) {
            return Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}