package com.tahai.wqltab;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            console.sendMessage(ChatColor.AQUA + "[WqlTab] PlaceholderAPI not found! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        scoreboardManager = new ScoreboardManager();
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();
                scoreboardManager.updateScoreboard(player);
                scoreboardManager.setTabList(player);
            }
        }, this);
        new RefreshTask(scoreboardManager).runTaskTimer(this, 0L, 20L);
        console.sendMessage(ChatColor.YELLOW + "[WqlTab] WqlTab has been enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[WqlTab] WqlTab has been disabled.");
    }
}