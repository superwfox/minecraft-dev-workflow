package com.tahai.lottery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LottoCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("admin")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Lottery");
            if (plugin != null) {
                String msg = plugin.getConfig().getString("console-only-command", "Only players can use this command.");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("lottery.admin")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Lottery");
            if (plugin != null) {
                String msg = plugin.getConfig().getString("no-permission-message", "You do not have permission.");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            } else {
                player.sendMessage("You do not have permission.");
            }
            return true;
        }

        GUIHolder guiHolder = new GUIHolder(GUIHolder.GUIType.ADMIN, null, new ArrayList<>(), s -> {});
        Inventory adminGUI = guiHolder.createAdminGUI();
        player.openInventory(adminGUI);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("admin".startsWith(args[0].toLowerCase())) {
                completions.add("admin");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}