package com.tahai.banitem;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);

        BanItemCommand banItemCommand = new BanItemCommand(databaseManager);
        getCommand("banitem").setExecutor(banItemCommand);
        getCommand("banitem").setTabCompleter(banItemCommand);

        getServer().getPluginManager().registerEvents(new BanItemListener(databaseManager), this);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}