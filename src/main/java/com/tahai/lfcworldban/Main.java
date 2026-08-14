package com.tahai.lfcworldban;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private BanManager banManager;
    private LFCWorldBanExpansion expansion;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.banManager = new BanManager(this);

        LFCWorldBanCommand command = new LFCWorldBanCommand(banManager);
        PluginCommand pluginCommand = getCommand("lfcworldban");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        getServer().getPluginManager().registerEvents(new BanListener(banManager), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.expansion = new LFCWorldBanExpansion(banManager);
            this.expansion.register();
        }
    }

    @Override
    public void onDisable() {
        if (banManager != null) {
            banManager.save();
            banManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public LFCWorldBanExpansion getLFCWorldBanExpansion() {
        return expansion;
    }
}