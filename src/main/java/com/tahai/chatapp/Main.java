package com.tahai.chatapp;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);

        getServer().getPluginManager().registerEvents(new ChatTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new ChatGUIListener(), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}