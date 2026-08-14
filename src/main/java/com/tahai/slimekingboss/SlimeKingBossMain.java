package com.tahai.slimekingboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class SlimeKingBossMain extends JavaPlugin implements CommandExecutor, TabCompleter {

    private ArrayList<SlimeKingBoss> bossList;

    @Override
    public void onEnable() {
        bossList = new ArrayList<>();
        getCommand("spawnslimeking").setExecutor(this);
        getCommand("spawnslimeking").setTabCompleter(this);

        // 在插件启用时注册 SlimeKingBoss 事件并启动任务
        // 使用默认世界出生点生成一个初始 BOSS，确保 onEnable 完成注册与定时任务启动
        if (!Bukkit.getWorlds().isEmpty()) {
            Location defaultLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            startBoss(defaultLocation);
        }
    }

    @Override
    public void onDisable() {
        for (SlimeKingBoss boss : bossList) {
            boss.destroy();
        }
        bossList.clear();
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

        startBoss(player.getLocation());
        player.sendMessage(ChatColor.YELLOW + "史莱姆王已生成!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }

    public void removeBossInstance(SlimeKingBoss boss) {
        bossList.remove(boss);
    }

    private void startBoss(Location location) {
        SlimeKingBoss boss = new SlimeKingBoss(location);
        bossList.add(boss);
        Bukkit.getPluginManager().registerEvents(boss, this);
        boss.runTaskTimer(this, 0L, 20L);
    }
}