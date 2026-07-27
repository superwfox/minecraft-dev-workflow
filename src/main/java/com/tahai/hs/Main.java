package com.tahai.hs;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private StatsManager statsManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        statsManager = new StatsManager(this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new HSExpansion().register();
        }
        PluginCommand hsCommand = getCommand("hs");
        HSCommand executor = new HSCommand();
        hsCommand.setExecutor(executor);
        hsCommand.setTabCompleter(executor);
        hsCommand.setAliases(getConfig().getStringList("aliases"));

        getServer().getPluginManager().registerEvents(new HSListener(), this);
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.shutdown();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}