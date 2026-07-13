package com.tahai.randomfishingloot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RfishingCommand implements CommandExecutor, TabCompleter {

    private static ConfigManager configManager;

    /**
     * 由插件主类在启用时调用，注入 ConfigManager 实例。
     */
    public static void setConfigManager(ConfigManager config) {
        configManager = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (configManager == null) {
            sender.sendMessage(ChatColor.AQUA + "Plugin not fully loaded (config unavailable).");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /rfishing <reload|give> [player]");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("randomfishing.reload")) {
                sender.sendMessage(ChatColor.AQUA + "You don't have permission to reload.");
                return true;
            }

            configManager.reload();
            sender.sendMessage(ChatColor.YELLOW + "Configuration reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("randomfishing.give")) {
                sender.sendMessage(ChatColor.AQUA + "You don't have permission to give items.");
                return true;
            }

            Player target;
            if (args.length >= 2) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.AQUA + "Player not found.");
                    return true;
                }
            } else {
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    sender.sendMessage(ChatColor.AQUA + "Console must specify a player.");
                    return true;
                }
            }

            RandomFishingLoot plugin = (RandomFishingLoot) Bukkit.getPluginManager().getPlugin("RandomFishingLoot");
            if (plugin == null) {
                sender.sendMessage(ChatColor.AQUA + "Plugin not running.");
                return true;
            }

            LootManager lootManager = plugin.getLootManager();
            List<String> pool = configManager.getEquipmentPool();
            int min = configManager.getEnchantCountMin();
            int max = configManager.getEnchantCountMax();
            ItemStack item = lootManager.generateRandomLoot(pool, min, max, new Random());
            target.getInventory().addItem(item);
            target.sendMessage(ChatColor.YELLOW + "You received a random fishing loot!");
            sender.sendMessage(ChatColor.YELLOW + "Given random fishing loot to " + target.getName());
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "Unknown subcommand. Use reload or give.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            subcommands.add("reload");
            subcommands.add("give");
            return subcommands;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> playerNames = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                playerNames.add(online.getName());
            }
            return playerNames;
        }
        return Collections.emptyList();
    }
}