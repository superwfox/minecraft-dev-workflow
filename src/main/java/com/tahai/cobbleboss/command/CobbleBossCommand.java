package com.tahai.cobbleboss.command;

import com.tahai.cobbleboss.config.ConfigManager;
import com.tahai.cobbleboss.manager.BossManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CobbleBossCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;
    private final BossManager bossManager;

    public CobbleBossCommand(ConfigManager configManager, BossManager bossManager) {
        this.configManager = configManager;
        this.bossManager = bossManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.AQUA + "Usage: /" + label + " spawn <bossId>");
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.AQUA + "Only players can spawn boss.");
                return true;
            }

            String bossId = args[1];
            if (configManager.getBossConfig(bossId) == null) {
                sender.sendMessage(configManager.getMessage("invalidBossId"));
                return true;
            }

            Player player = (Player) sender;
            Location location = player.getLocation();
            bossManager.spawnBoss(bossId, location);
            sender.sendMessage(configManager.getMessage("bossSpawn"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            configManager.reload();
            bossManager.updateBossesFromConfig();
            sender.sendMessage(configManager.getMessage("reloadComplete"));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("spawn");
            completions.add("reload");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("CobbleBoss");
            if (plugin == null) {
                return Collections.emptyList();
            }
            FileConfiguration config = plugin.getConfig();
            List<Map<?, ?>> bosses = config.getMapList("bosses");
            List<String> ids = new ArrayList<>();
            for (Map<?, ?> boss : bosses) {
                Object idObj = boss.get("bossId");
                if (idObj instanceof String) {
                    ids.add((String) idObj);
                }
            }
            return ids;
        }

        return Collections.emptyList();
    }
}