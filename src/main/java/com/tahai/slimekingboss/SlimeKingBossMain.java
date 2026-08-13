package com.tahai.slimekingboss;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlimeKingBossMain extends JavaPlugin implements CommandExecutor, TabCompleter {

    private final List<SlimeKingBoss> bosses = new ArrayList<>();

    @Override
    public void onEnable() {
        getCommand("spawnslimeking").setExecutor(this);
        getCommand("spawnslimeking").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        for (SlimeKingBoss boss : new ArrayList<>(bosses)) {
            boss.destroy();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("slimekingboss.spawn")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限生成史莱姆王.");
            return true;
        }
        SlimeKingBoss boss = new SlimeKingBoss(player.getLocation());
        boss.spawnBoss();
        getServer().getPluginManager().registerEvents(boss, this);
        addBossInstance(boss);
        boss.runTaskTimer(this, 0, 20);
        player.sendMessage(ChatColor.YELLOW + "史莱姆王已生成!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    public void addBossInstance(SlimeKingBoss boss) {
        bosses.add(boss);
    }

    public void removeBossInstance(SlimeKingBoss boss) {
        bosses.remove(boss);
    }
}