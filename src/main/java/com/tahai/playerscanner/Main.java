package com.tahai.playerscanner;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        this.dataManager = new DataManager(this);

        ScanCommand scanCommand = new ScanCommand();
        getCommand("scan").setExecutor(scanCommand);
        getCommand("scan").setTabCompleter(scanCommand);

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}