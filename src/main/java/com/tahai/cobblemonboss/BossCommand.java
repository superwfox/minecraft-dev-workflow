package com.tahai.cobblemonboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class BossCommand implements CommandExecutor, TabCompleter {

    private BossManager getBossManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CobblemonBoss");
        if (plugin == null) return null;
        try {
            Field field = plugin.getClass().getDeclaredField("bossManager");
            field.setAccessible(true);
            return (BossManager) field.get(plugin);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to access BossManager: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("boss.cmd")) {
            sender.sendMessage(ChatColor.AQUA + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /boss <setpoint|reload> ...");
            return true;
        }

        String sub = args[0].toLowerCase();
        BossManager bossManager = getBossManager();
        if (bossManager == null) {
            sender.sendMessage(ChatColor.GRAY + "BossManager is not available.");
            return true;
        }

        switch (sub) {
            case "setpoint":
                handleSetPoint(sender, args, bossManager);
                break;
            case "reload":
                handleReload(sender, args, bossManager);
                break;
            default:
                sender.sendMessage(ChatColor.GRAY + "Unknown subcommand. Use: setpoint, reload");
        }
        return true;
    }

    private void handleSetPoint(CommandSender sender, String[] args, BossManager bossManager) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /boss setpoint <boss> [x y z]");
            return;
        }
        String bossId = args[1];
        if (bossManager.getBossTemplate(bossId) == null) {
            sender.sendMessage(ChatColor.AQUA + "Boss template '" + bossId + "' not found.");
            return;
        }

        Location loc;
        if (args.length == 5) {
            try {
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                if (sender instanceof Player) {
                    loc = new Location(((Player) sender).getWorld(), x, y, z);
                } else {
                    loc = new Location(Bukkit.getWorlds().get(0), x, y, z);
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.AQUA + "Invalid coordinates. Use numbers for x y z.");
                return;
            }
        } else if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.AQUA + "You must specify coordinates when running from console.");
                return;
            }
            loc = ((Player) sender).getLocation();
        } else {
            sender.sendMessage(ChatColor.GRAY + "Usage: /boss setpoint <boss> [x y z]");
            return;
        }

        bossManager.setSpawnPoint(bossId, loc);
        bossManager.save();
        sender.sendMessage(ChatColor.YELLOW + "Spawn point for boss '" + bossId + "' set to " +
                loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
    }

    private void handleReload(CommandSender sender, String[] args, BossManager bossManager) {
        boolean refresh = args.length >= 2 && args[1].equalsIgnoreCase("refresh");
        boolean soft = args.length >= 2 && args[1].equalsIgnoreCase("soft");
        if (!refresh && !soft) {
            soft = true;
        }

        if (refresh) {
            for (String id : bossManager.getAllBossTemplates().keySet()) {
                bossManager.setAlive(id, false);
            }
            sender.sendMessage(ChatColor.YELLOW + "All boss states cleared (refresh). They will respawn based on their timers.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Configuration reloaded (soft). In-memory templates unchanged; restart to fully apply changes.");
        }
        bossManager.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("boss.cmd")) {
            return Collections.emptyList();
        }
        BossManager bossManager = getBossManager();
        if (args.length == 1) {
            List<String> subs = Arrays.asList("setpoint", "reload");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if ("setpoint".equals(sub)) {
                if (bossManager != null) {
                    Set<String> ids = bossManager.getAllBossTemplates().keySet();
                    return ids.stream().filter(id -> id.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                return Collections.emptyList();
            } else if ("reload".equals(sub)) {
                List<String> options = Arrays.asList("refresh", "soft");
                return options.stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("setpoint")) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}