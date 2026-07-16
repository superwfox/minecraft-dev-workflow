package com.tahai.playerauth;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;
    private PlayerAuthListener playerAuthListener;
    private AuthCommand authCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataManager = new DataManager(this);
        playerAuthListener = new PlayerAuthListener();
        authCommand = new AuthCommand();

        getCommand("register").setExecutor(authCommand);
        getCommand("register").setTabCompleter(authCommand);
        getCommand("login").setExecutor(authCommand);
        getCommand("login").setTabCompleter(authCommand);

        getServer().getPluginManager().registerEvents(playerAuthListener, this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}