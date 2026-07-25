package com.tahai.baoshi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GemCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "baoshi.admin";
    private static final String[] SUB_COMMANDS = {"reload", "give"};
    private static final String[] TYPE_CANDIDATES = {"RUBY", "EMERALD", "DIAMOND", "SAPPHIRE", "glue"};

    private static YamlConfiguration messagesConfig;

    public static void initMessages(YamlConfiguration config) {
        messagesConfig = config;
    }

    private String getMessage(String path, String defaultMessage) {
        if (messagesConfig != null && messagesConfig.contains(path)) {
            return messagesConfig.getString(path);
        }
        return defaultMessage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.AQUA + getMessage("no-permission", "你没有权限执行此命令。"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + getMessage("usage-main", "用法: /gem <reload|give>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Baoshi");
            if (plugin == null) {
                sender.sendMessage(ChatColor.AQUA + getMessage("plugin-not-found", "无法获取插件实例。"));
                return true;
            }
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            if (!messagesFile.exists()) {
                sender.sendMessage(ChatColor.AQUA + getMessage("messages-file-missing", "messages.yml 文件不存在。"));
                return true;
            }
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            sender.sendMessage(ChatColor.YELLOW + getMessage("messages-reloaded", "messages.yml 已重新加载。"));
            return true;
        }

        if (sub.equals("give")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.GRAY + getMessage("usage-give", "用法: /gem give <玩家> <类型> [等级] [数量]"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.AQUA + getMessage("player-offline", "玩家 " + args[1] + " 不在线。"));
                return true;
            }

            String typeStr = args[2].toLowerCase();
            ItemStack item;

            if (typeStr.equals("glue")) {
                item = GemHelper.createGlueItem();
            } else {
                GemHelper.GemType type;
                try {
                    type = GemHelper.GemType.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.AQUA + getMessage("invalid-type", "无效的宝石类型: " + args[2] + "。可用类型: RUBY, EMERALD, DIAMOND, SAPPHIRE, glue"));
                    return true;
                }

                int level = 1;
                if (args.length >= 4) {
                    try {
                        level = Integer.parseInt(args[3]);
                        if (level < 1) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.AQUA + getMessage("invalid-level", "等级必须为正整数。"));
                        return true;
                    }
                }

                item = GemHelper.createGemItem(type, level);
            }

            int amount = 1;
            if (args.length >= 5) {
                try {
                    amount = Integer.parseInt(args[4]);
                    if (amount < 1) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.AQUA + getMessage("invalid-amount", "数量必须为正整数。"));
                    return true;
                }
            }

            item.setAmount(amount);

            target.getInventory().addItem(item);
            String giveMessage = getMessage("give-success", "已给予 " + target.getName() + " " + amount + " 个 " + (typeStr.equals("glue") ? "粘合剂" : args[2]) + "。");
            sender.sendMessage(ChatColor.YELLOW + giveMessage);
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + getMessage("unknown-sub", "未知子命令: " + args[0] + "。可用: reload, give"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Arrays.stream(SUB_COMMANDS)
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String prefix = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            String prefix = args[2].toLowerCase();
            return Arrays.stream(TYPE_CANDIDATES)
                    .filter(s -> s.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            String type = args[2].toUpperCase();
            if (type.equals("GLUE")) {
                return Collections.emptyList();
            }
            return Collections.singletonList("1");
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("give")) {
            String type = args[2].toUpperCase();
            if (type.equals("GLUE")) {
                return Collections.emptyList();
            }
            return Collections.singletonList("1");
        }

        return Collections.emptyList();
    }
}