package com.tahai.scanmanager;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static DataManager dataManager;

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);
        dataManager.load();

        ScanCommand scanCommand = new ScanCommand();
        getCommand("scan").setExecutor(scanCommand);
        getCommand("scan").setTabCompleter(scanCommand);

        getServer().getPluginManager().registerEvents(new JoinListener(dataManager), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public static DataManager getDataManager() {
        return dataManager;
    }
}