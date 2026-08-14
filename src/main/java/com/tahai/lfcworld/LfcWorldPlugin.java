package com.tahai.lfcworld;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class LfcWorldPlugin extends JavaPlugin {

    private BanManager banManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.banManager = new BanManager();

        PluginCommand command = getCommand("lfcworld");
        LfcWorldCommand executor = new LfcWorldCommand();
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        getServer().getPluginManager().registerEvents(new BanCheckListener(banManager), this);
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
}