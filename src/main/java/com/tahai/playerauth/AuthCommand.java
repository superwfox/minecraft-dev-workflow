package com.tahai.playerauth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class AuthCommand implements CommandExecutor, TabCompleter {

    private static DataManager dataManager;

    private static DataManager getDataManager() {
        if (dataManager == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerAuth");
            dataManager = new DataManager(plugin);
        }
        return dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName();

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Permission check
        if (!sender.hasPermission("playerauth." + cmdName)) {
            sender.sendMessage(ChatColor.AQUA + "You don't have permission to use this command.");
            return true;
        }

        DataManager dm = getDataManager();

        if (cmdName.equalsIgnoreCase("register")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.GRAY + "Usage: /register <password> <confirmPassword>");
                return true;
            }

            String password = args[0];
            String confirmPassword = args[1];

            // Password length check (handled by DataManager, but we can shortcut)
            if (password.length() < 6) {
                sender.sendMessage(ChatColor.AQUA + dm.getPasswordTooShort());
                return true;
            }

            String result = dm.register(player.getName(), password, confirmPassword);
            sender.sendMessage(result);
            return true;
        }

        if (cmdName.equalsIgnoreCase("login")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.GRAY + "Usage: /login <password>");
                return true;
            }

            String password = args[0];
            boolean success = dm.login(player.getUniqueId(), player.getName(), password);
            if (success) {
                sender.sendMessage(ChatColor.YELLOW + dm.getLoginSuccess());
            } else {
                sender.sendMessage(ChatColor.AQUA + dm.getWrongPassword());
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // No tab completion for passwords
        return Collections.emptyList();
    }
}