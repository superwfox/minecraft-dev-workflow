package com.tahai.hookwhitelist.whitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HookWhitelistCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final WhitelistManager whitelistManager;

    public HookWhitelistCommand(Plugin plugin, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;
        clearCommandPermission();
    }

    /**
     * 命令契约中 permission 为空（任何玩家都可执行 list 等子命令）。
     * plugin.yml 中若残留了 hookwhitelist.use 之类的权限声明（默认仅 OP），
     * 普通玩家会在进入 onCommand 之前就被 Bukkit 拦截，因此这里清空命令的权限要求，
     * 保证 /hookwl list、/hwl list 等路径对所有玩家可达。
     * 仅清空权限，不在此处注册 executor / tabCompleter（注册由 Main.onEnable 统一完成）。
     */
    private void clearCommandPermission() {
        try {
            PluginCommand command = plugin.getCommand("hookwl");
            if (command != null) {
                command.setPermission(null);
            }
        } catch (Throwable ignored) {
            // 命令尚未注册时忽略，不影响其余功能
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add" -> {
                if (args.length < 2) {
                    sendUsage(sender);
                    return true;
                }
                handleAdd(sender, args[1]);
            }
            case "remove" -> {
                if (args.length < 2) {
                    sendUsage(sender);
                    return true;
                }
                handleRemove(sender, args[1]);
            }
            case "list" -> handleList(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleAdd(CommandSender sender, String name) {
        Player online = Bukkit.getPlayerExact(name);
        UUID uuid;
        String resolvedName;
        if (online != null) {
            uuid = online.getUniqueId();
            resolvedName = online.getName();
        } else {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            if (offline == null || !offline.hasPlayedBefore() || offline.getUniqueId() == null) {
                sender.sendMessage(ChatColor.AQUA + "该玩家不存在或名字拼写错误");
                return;
            }
            uuid = offline.getUniqueId();
            String offlineName = offline.getName();
            resolvedName = offlineName != null ? offlineName : name;
        }

        whitelistManager.add(uuid, resolvedName);
        whitelistManager.save();
        sender.sendMessage(ChatColor.YELLOW + "已将玩家 " + ChatColor.BOLD + resolvedName
                + ChatColor.YELLOW + " 加入白名单");
    }

    private void handleRemove(CommandSender sender, String name) {
        Player online = Bukkit.getPlayerExact(name);
        UUID uuid;
        String resolvedName;
        if (online != null) {
            uuid = online.getUniqueId();
            resolvedName = online.getName();
        } else {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            if (offline == null || !offline.hasPlayedBefore() || offline.getUniqueId() == null) {
                sender.sendMessage(ChatColor.AQUA + "该玩家不存在或名字拼写错误");
                return;
            }
            uuid = offline.getUniqueId();
            String offlineName = offline.getName();
            resolvedName = offlineName != null ? offlineName : name;
        }

        if (!whitelistManager.contains(uuid)) {
            sender.sendMessage(ChatColor.AQUA + "玩家 " + resolvedName + " 不在白名单中");
            return;
        }

        whitelistManager.remove(uuid);
        whitelistManager.save();
        sender.sendMessage(ChatColor.YELLOW + "已将玩家 " + ChatColor.BOLD + resolvedName
                + ChatColor.YELLOW + " 从白名单移除");
    }

    private void handleList(CommandSender sender) {
        List<String> names = whitelistManager.getNames();
        if (names == null || names.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "白名单为空");
            return;
        }
        sender.sendMessage(ChatColor.GRAY + "白名单玩家（共 " + names.size() + " 人）：");
        for (String name : names) {
            sender.sendMessage(ChatColor.YELLOW + " - " + name);
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "用法：");
        sender.sendMessage(ChatColor.GRAY + "  /hookwl add <玩家名>");
        sender.sendMessage(ChatColor.GRAY + "  /hookwl remove <玩家名>");
        sender.sendMessage(ChatColor.GRAY + "  /hookwl list");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String s : List.of("add", "remove", "list")) {
                if (s.startsWith(prefix)) {
                    subs.add(s);
                }
            }
            return subs;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("add") || sub.equals("remove")) {
                List<String> result = new ArrayList<>();
                String prefix = args[1].toLowerCase(Locale.ROOT);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        result.add(p.getName());
                    }
                }
                return result;
            }
        }
        return Collections.emptyList();
    }
}