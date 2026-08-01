package com.tahai.sect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SectCommand implements CommandExecutor, TabCompleter {

    private final SectDataManager dataManager;

    public SectCommand(SectDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sect.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令。");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "用法: /sect <create|gui|disband|war|reload>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(sender, args);
            case "gui" -> handleGui(sender);
            case "disband" -> handleDisband(sender, args);
            case "war" -> handleWar(sender, args);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(ChatColor.AQUA + "未知子命令，使用 /sect <create|gui|disband|war|reload>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"create", "gui", "disband", "war", "reload"}) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("war")) {
                for (String sub : new String[]{"start", "accept", "end"}) {
                    if (sub.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            } else if (args[0].equalsIgnoreCase("disband")) {
                for (String name : dataManager.getSects().keySet()) {
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(name);
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("war") && args[1].equalsIgnoreCase("start")) {
            for (String name : dataManager.getSects().keySet()) {
                if (name.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(name);
                }
            }
        }
        return completions.isEmpty() ? Collections.emptyList() : completions;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.GRAY + "用法: /sect create <宗门名称>");
            return;
        }
        if (dataManager.createSect(args[1], player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "宗门 " + ChatColor.BOLD + args[1] + ChatColor.YELLOW + " 创建成功！");
        } else {
            player.sendMessage(ChatColor.AQUA + "宗门创建失败，可能名称已存在或你已在其他宗门。");
        }
    }

    private void handleGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return;
        }
        SectGUI.openMainMenu(player, dataManager).open(player);
    }

    private void handleDisband(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return;
        }
        String sectName = args.length >= 2 ? args[1] : getPlayerSect(player.getUniqueId());
        if (sectName == null) {
            player.sendMessage(ChatColor.AQUA + "你不在任何宗门中。");
            return;
        }
        if (dataManager.disbandSect(sectName, player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "宗门 " + ChatColor.BOLD + sectName + ChatColor.YELLOW + " 已解散。");
        } else {
            player.sendMessage(ChatColor.AQUA + "解散失败，请确认你是宗主。");
        }
    }

    private void handleWar(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.GRAY + "用法: /sect war <start|accept|end>");
            return;
        }
        String playerSect = getPlayerSect(player.getUniqueId());
        switch (args[1].toLowerCase()) {
            case "start" -> {
                if (playerSect == null) {
                    player.sendMessage(ChatColor.AQUA + "你不在任何宗门中。");
                    return;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.GRAY + "用法: /sect war start <防守方宗门>");
                    return;
                }
                if (dataManager.startWar(playerSect, args[2], player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "已向宗门 " + ChatColor.BOLD + args[2] + ChatColor.YELLOW + " 发起战争！");
                } else {
                    player.sendMessage(ChatColor.AQUA + "开战失败，请检查双方宗门是否存在。");
                }
            }
            case "accept" -> {
                if (playerSect == null) {
                    player.sendMessage(ChatColor.AQUA + "你不在任何宗门中。");
                    return;
                }
                if (dataManager.acceptWar(playerSect, player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "战争已接受，战斗开始！");
                } else {
                    player.sendMessage(ChatColor.AQUA + "接受失败，没有待接受的战争。");
                }
            }
            case "end" -> {
                if (playerSect == null) {
                    player.sendMessage(ChatColor.AQUA + "你不在任何宗门中。");
                    return;
                }
                if (dataManager.endWar(playerSect, player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "战争已结束。");
                } else {
                    player.sendMessage(ChatColor.AQUA + "结束失败，请确认你是宗主。");
                }
            }
            default -> player.sendMessage(ChatColor.AQUA + "未知战争操作。");
        }
    }

    private void handleReload(CommandSender sender) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未找到，无法重载。");
            return;
        }
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.YELLOW + "配置已重新加载。");
    }

    private String getPlayerSect(UUID playerId) {
        for (Sect sect : dataManager.getSects().values()) {
            if (sect.isMember(playerId)) {
                return sect.getName();
            }
        }
        return null;
    }
}