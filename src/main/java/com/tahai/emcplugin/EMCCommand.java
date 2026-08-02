package com.tahai.emcplugin;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class EMCCommand implements CommandExecutor, TabCompleter {

    private final DataManager dataManager;

    public EMCCommand() {
        this.dataManager = new DataManager();
        this.dataManager.loadValues();
        this.dataManager.loadPoints();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "Please run this command as a player.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("emc.use")) {
            player.sendMessage(ChatColor.AQUA + "You do not have permission to use this command.");
            return true;
        }

        GUIHolder holder = GUIHolder.createMainMenu(dataManager);
        holder.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}