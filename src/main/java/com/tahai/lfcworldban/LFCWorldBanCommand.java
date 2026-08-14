package com.tahai.lfcworldban;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LFCWorldBanCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MM = MiniMessage.builder().legacyAmpersand(true).build();
    private final BanManager banManager;

    public LFCWorldBanCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lfcworldban.admin")) {
            sendMessage(sender, "messages.no_permission", "&bYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, "messages.usage", "&7Usage: /lfcworldban reload|add <world> <material>");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            banManager.reload();
            for (Player player : Bukkit.getOnlinePlayers()) {
                banManager.checkPlayer(player);
            }
            sendMessage(sender, "messages.reload_success", "&eConfiguration reloaded. All online players rechecked.");
            return true;
        }

        if (sub.equals("add")) {
            if (args.length < 3) {
                sendMessage(sender, "messages.add_usage", "&7Usage: /lfcworldban add <world> <material>");
                return true;
            }

            Material material = Material.matchMaterial(args[2]);
            if (material == null) {
                sendMessage(sender, "messages.invalid_material", "&bInvalid material: {material}", "{material}", args[2]);
                return true;
            }

            banManager.addBannedItem(args[1], material);
            banManager.save();
            sendMessage(sender, "messages.add_success",
                    "&eAdded &f{item}&e to banned items for world &f{world}&e.",
                    "{world}", args[1],
                    "{item}", material.name());
            return true;
        }

        sendMessage(sender, "messages.usage", "&7Usage: /lfcworldban reload|add <world> <material>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("lfcworldban.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("reload", "add");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            return Arrays.stream(Material.values())
                    .map(Material::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private void sendMessage(CommandSender sender, String path, String def, String... replacements) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LFCWorldBan");
        if (plugin == null) return;

        String msg = plugin.getConfig().getString(path, def);
        if (msg == null || msg.isEmpty()) return;

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }

        sender.sendMessage(MM.deserialize(msg));
    }
}