package com.tahai.emcplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataManager = new DataManager();
        dataManager.loadValues();
        dataManager.loadPoints();

        EMCCommand emcCommand = new EMCCommand();
        getCommand("emc").setExecutor(emcCommand);
        getCommand("emc").setTabCompleter(emcCommand);

        getServer().getPluginManager().registerEvents(new GUIListener(), this);
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