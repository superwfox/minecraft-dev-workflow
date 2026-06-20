package com.tahai;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoinManager {

    private static final Map<UUID, Integer> balances = new HashMap<>();
    private static JavaPlugin plugin;

    public static void init(JavaPlugin plugin) {
        CoinManager.plugin = plugin;
        loadAll();
    }

    public static int getCoins(UUID uuid) {
        return balances.getOrDefault(uuid, 0);
    }

    public static void setCoins(UUID uuid, int amount) {
        balances.put(uuid, Math.max(0, amount));
        save(uuid);
    }

    public static void addCoins(UUID uuid, int amount) {
        setCoins(uuid, getCoins(uuid) + amount);
    }

    public static void removeCoins(UUID uuid, int amount) {
        setCoins(uuid, getCoins(uuid) - amount);
    }

    private static void loadAll() {
        FileConfiguration config = plugin.getConfig();
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                balances.put(uuid, config.getInt(key));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private static void save(UUID uuid) {
        plugin.getConfig().set(uuid.toString(), balances.get(uuid));
        plugin.saveConfig();
    }

    public static class CoinCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.YELLOW + "用法: /coin <玩家> 或 /coin give/take/set <玩家> <数量>");
                return true;
            }

            // /coin <player> 查询
            if (args.length == 1) {
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "玩家不在线或不存在");
                    return true;
                }
                int coins = getCoins(target.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + target.getName() + " 的金币: " + coins);
                return true;
            }

            // 需要管理员权限的操作
            if (!sender.hasPermission("coin.admin")) {
                sender.sendMessage(ChatColor.RED + "你没有权限执行此命令");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(ChatColor.YELLOW + "用法: /coin give/take/set <玩家> <数量>");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "玩家不在线或不存在");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "数量必须是非负整数");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "give":
                    addCoins(target.getUniqueId(), amount);
                    sender.sendMessage(ChatColor.GREEN + "给予 " + target.getName() + " " + amount + " 金币");
                    break;
                case "take":
                    removeCoins(target.getUniqueId(), amount);
                    sender.sendMessage(ChatColor.GREEN + "从 " + target.getName() + " 扣除 " + amount + " 金币");
                    break;
                case "set":
                    setCoins(target.getUniqueId(), amount);
                    sender.sendMessage(ChatColor.GREEN + "设置 " + target.getName() + " 的金币为 " + amount);
                    break;
                default:
                    sender.sendMessage(ChatColor.YELLOW + "未知子命令，可用: give, take, set");
            }
            return true;
        }
    }
}