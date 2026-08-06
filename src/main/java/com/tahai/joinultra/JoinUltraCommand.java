package com.tahai.joinultra;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class JoinUltraCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("joinultra.reload")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<aqua>You do not have permission to use this command.</aqua>"
            ));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("JoinUltra");
            new ConfigManager(plugin).reloadConfig();
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<yellow>Configuration reloaded successfully.</yellow>"
            ));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}