package com.tahai.prankplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private PrankTaskManager prankTaskManager;

    @Override
    public void onEnable() {
        // Step 1: save default config
        saveDefaultConfig();

        // Step 2: instantiate services (lifecycle = onEnable)
        prankTaskManager = new PrankTaskManager();

        // Step 3: register command
        PluginCommand prankCommand = getCommand("prank");
        if (prankCommand != null) {
            PrankCommand executor = new PrankCommand();
            prankCommand.setExecutor(executor);
            prankCommand.setTabCompleter(executor);
        }

        // Step 4: register listeners
        getServer().getPluginManager().registerEvents(new ReverseMoveListener(), this);
        getServer().getPluginManager().registerEvents(new PrankGuiListener(), this);

        // Step 5: no scheduled tasks
    }

    @Override
    public void onDisable() {
        if (prankTaskManager != null) {
            prankTaskManager.shutdown();
            prankTaskManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public PrankTaskManager getPrankTaskManager() {
        return prankTaskManager;
    }
}