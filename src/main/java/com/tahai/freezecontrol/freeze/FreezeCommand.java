package com.tahai.freezecontrol.freeze;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
import java.util.UUID;

public class FreezeCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "freezecontrol.admin";
    private static final List<String> SUBCOMMANDS = Arrays.asList("on", "off", "add", "remove", "list");

    private final Plugin plugin;
    private final FreezeManager freezeManager;

    public FreezeCommand(Plugin plugin, FreezeManager freezeManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用该指令。");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "on":
                freezeManager.setEnabled(true);
                sender.sendMessage(ChatColor.YELLOW + "全局冻结已开启，当前状态：" + state());
                return true;
            case "off":
                freezeManager.setEnabled(false);
                sender.sendMessage(ChatColor.YELLOW + "全局冻结已关闭，当前状态：" + state());
                return true;
            case "add": {
                if (args.length < 2) {
                    sendUsage(sender, label);
                    return true;
                }
                UUID uuid = resolveUuid(args[1]);
                String name = resolveName(args[1]);
                if (freezeManager.addWhitelist(uuid)) {
                    sender.sendMessage(ChatColor.YELLOW + name + " 已加入白名单。");
                } else {
                    sender.sendMessage(ChatColor.AQUA + name + " 已在白名单中。");
                }
                return true;
            }
            case "remove": {
                if (args.length < 2) {
                    sendUsage(sender, label);
                    return true;
                }
                UUID uuid = resolveUuid(args[1]);
                String name = resolveName(args[1]);
                if (freezeManager.removeWhitelist(uuid)) {
                    sender.sendMessage(ChatColor.YELLOW + name + " 已移出白名单。");
                } else {
                    sender.sendMessage(ChatColor.AQUA + name + " 不在白名单中。");
                }
                return true;
            }
            case "list": {
                List<String> names = freezeManager.getWhitelistNames();
                if (names.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "白名单为空。");
                } else {
                    sender.sendMessage(ChatColor.GRAY + "白名单（" + names.size() + "）："
                            + ChatColor.YELLOW + String.join(", ", names));
                }
                return true;
            }
            default:
                sendUsage(sender, label);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add") || sub.equals("remove")) {
                List<String> names = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    names.add(player.getName());
                }
                if (sub.equals("remove")) {
                    for (String name : freezeManager.getWhitelistNames()) {
                        if (!names.contains(name)) {
                            names.add(name);
                        }
                    }
                }
                return filter(names, args[1]);
            }
        }
        return Collections.emptyList();
    }

    private String state() {
        return freezeManager.isEnabled() ? "已开启" : "已关闭";
    }

    private UUID resolveUuid(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        return offline.getUniqueId();
    }

    private String resolveName(String name) {
        Player online = Bukkit.getPlayerExact(name);
        return online != null ? online.getName() : name;
    }

    private List<String> filter(List<String> candidates, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(lower)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GRAY + "用法：/" + label + " <on|off|add|remove|list> [player]");
    }
}