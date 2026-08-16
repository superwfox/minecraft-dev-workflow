package com.tahai.slimeboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class SpawnSlimeKingCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender == null) {
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GRAY + "该命令只能由玩家执行。");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("slimeboss.spawn")) {
            player.sendMessage(ChatColor.GRAY + "你没有权限执行该命令。");
            return true;
        }
        Location location = player.getLocation();
        if (location == null) {
            player.sendMessage(ChatColor.GRAY + "无法获取你的位置。");
            return true;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SlimeBoss");
        if (plugin == null) {
            player.sendMessage(ChatColor.GRAY + "SlimeBoss 插件未加载。");
            return true;
        }
        Slime boss = spawnSlimeKing(location, plugin);
        if (boss != null) {
            player.sendMessage(ChatColor.YELLOW + "已生成史莱姆王！");
        } else {
            player.sendMessage(ChatColor.AQUA + "生成史莱姆王失败。");
        }
        return true;
    }

    public Slime spawnSlimeKing(Location location, Plugin plugin) {
        if (location == null || plugin == null || location.getWorld() == null) {
            return null;
        }
        Slime slime = location.getWorld().spawn(location, Slime.class);
        slime.setSize(10);
        slime.setMaxHealth(2000.0);
        slime.setHealth(2000.0);
        slime.setCustomName(ChatColor.GREEN + "史莱姆王");
        slime.setCustomNameVisible(true);
        SlimeBossAI ai = new SlimeBossAI(slime, plugin);
        plugin.getServer().getPluginManager().registerEvents(ai, plugin);
        ai.runTaskTimer(plugin, 0L, 1L);
        return slime;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}