package com.tahai.sellvaultgui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SellCommands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("sellgui")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(getMessage("only-player", ChatColor.GRAY + "Only players can use this command."));
                return true;
            }
            Player player = (Player) sender;
            SellGui gui = new SellGui().create();
            gui.open(player);
            return true;
        }

        if (cmd.equals("reloadsellgui")) {
            if (!sender.isOp()) {
                sender.sendMessage(getMessage("no-permission", ChatColor.AQUA + "You don't have permission to use this command."));
                return true;
            }
            Plugin plugin = Bukkit.getPluginManager().getPlugin("SellVaultGui");
            if (plugin == null) {
                return true;
            }
            new PriceManager(plugin).reload();
            sender.sendMessage(getMessage("reload-success", ChatColor.YELLOW + "Configuration reloaded."));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("sellgui", "reloadsellgui");
        }
        return Collections.emptyList();
    }

    private String getMessage(String path, String def) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SellVaultGui");
        if (plugin == null) {
            return def;
        }
        File file = new File(plugin.getDataFolder(), "messages.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String msg = config.getString(path, def);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}