package com.tahai.unpc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnpcCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("unpc.admin")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Unpc");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未正确加载。");
            return true;
        }
        NpcManager manager = NpcManager.fromPlugin(plugin);
        if (manager == null) {
            sender.sendMessage(ChatColor.AQUA + "NPC 管理器不可用。");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                break;
            case "create":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.AQUA + "用法: /unpc create <名字>");
                    break;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
                    break;
                }
                String name = args[1];
                if (manager.getNpc(name) != null) {
                    sender.sendMessage(ChatColor.AQUA + "名为 " + name + " 的 NPC 已存在。");
                    break;
                }
                manager.createNpc(name, player.getName(), Collections.emptyList(), player.getLocation());
                sender.sendMessage(ChatColor.YELLOW + "NPC " + ChatColor.BOLD + name + ChatColor.YELLOW + " 已创建。");
                break;
            case "setcommand":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.AQUA + "用法: /unpc setcommand <NPC名> <命令...>");
                    break;
                }
                String npcName = args[1];
                if (manager.getNpc(npcName) == null) {
                    sender.sendMessage(ChatColor.AQUA + "NPC " + npcName + " 不存在。");
                    break;
                }
                String joined = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                List<String> commands = new ArrayList<>();
                for (String cmd : joined.split(";")) {
                    String trimmed = cmd.trim();
                    if (!trimmed.isEmpty()) {
                        commands.add(trimmed);
                    }
                }
                if (commands.isEmpty()) {
                    sender.sendMessage(ChatColor.AQUA + "未提供有效命令。");
                    break;
                }
                manager.setCommands(npcName, commands);
                sender.sendMessage(ChatColor.YELLOW + "已设置 NPC " + npcName + " 的命令。");
                break;
            case "setskin":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.AQUA + "用法: /unpc setskin <NPC名> <正版皮肤名>");
                    break;
                }
                String skinNpc = args[1];
                if (manager.getNpc(skinNpc) == null) {
                    sender.sendMessage(ChatColor.AQUA + "NPC " + skinNpc + " 不存在。");
                    break;
                }
                manager.setSkin(skinNpc, args[2]);
                sender.sendMessage(ChatColor.YELLOW + "已设置 NPC " + skinNpc + " 的皮肤。");
                break;
            case "setname":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.AQUA + "用法: /unpc setname <NPC名> <新名字>");
                    break;
                }
                String oldName = args[1];
                if (manager.getNpc(oldName) == null) {
                    sender.sendMessage(ChatColor.AQUA + "NPC " + oldName + " 不存在。");
                    break;
                }
                manager.renameNpc(oldName, args[2]);
                sender.sendMessage(ChatColor.YELLOW + "NPC " + oldName + " 已改名为 " + args[2] + "。");
                break;
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.AQUA + "用法: /unpc delete <NPC名>");
                    break;
                }
                String delNpc = args[1];
                if (manager.getNpc(delNpc) == null) {
                    sender.sendMessage(ChatColor.AQUA + "NPC " + delNpc + " 不存在。");
                    break;
                }
                manager.removeNpc(delNpc);
                sender.sendMessage(ChatColor.YELLOW + "NPC " + delNpc + " 已删除。");
                break;
            case "tphere":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.AQUA + "用法: /unpc tphere <NPC名>");
                    break;
                }
                if (!(sender instanceof Player targetPlayer)) {
                    sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
                    break;
                }
                String tpNpc = args[1];
                if (manager.getNpc(tpNpc) == null) {
                    sender.sendMessage(ChatColor.AQUA + "NPC " + tpNpc + " 不存在。");
                    break;
                }
                manager.teleportNpc(tpNpc, targetPlayer.getLocation());
                sender.sendMessage(ChatColor.YELLOW + "NPC " + tpNpc + " 已传送到你的位置。");
                break;
            default:
                sender.sendMessage(ChatColor.AQUA + "未知子命令，使用 /unpc help 查看帮助。");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help", "create", "setcommand", "setskin", "setname", "delete", "tphere");
            return filter(subcommands, args[0]);
        }
        if (args.length == 2 && isNpcCommand(args[0])) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private boolean isNpcCommand(String arg) {
        return arg.equalsIgnoreCase("setcommand") || arg.equalsIgnoreCase("setskin")
                || arg.equalsIgnoreCase("setname") || arg.equalsIgnoreCase("delete")
                || arg.equalsIgnoreCase("tphere");
    }

    private List<String> filter(List<String> list, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return list;
        }
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "===== Unpc 命令帮助 =====");
        sender.sendMessage(ChatColor.YELLOW + "/unpc help" + ChatColor.GRAY + " - 显示本帮助");
        sender.sendMessage(ChatColor.YELLOW + "/unpc create <名字>" + ChatColor.GRAY + " - 在当前位置创建 NPC");
        sender.sendMessage(ChatColor.YELLOW + "/unpc setcommand <NPC名> <命令...>" + ChatColor.GRAY + " - 设置右键执行的命令（用分号分隔多条）");
        sender.sendMessage(ChatColor.YELLOW + "/unpc setskin <NPC名> <正版皮肤名>" + ChatColor.GRAY + " - 设置皮肤");
        sender.sendMessage(ChatColor.YELLOW + "/unpc setname <NPC名> <新名字>" + ChatColor.GRAY + " - 修改 NPC 名字");
        sender.sendMessage(ChatColor.YELLOW + "/unpc delete <NPC名>" + ChatColor.GRAY + " - 删除 NPC");
        sender.sendMessage(ChatColor.YELLOW + "/unpc tphere <NPC名>" + ChatColor.GRAY + " - 将 NPC 传送到当前位置");
    }
}