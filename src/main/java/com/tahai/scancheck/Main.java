package com.tahai.scancheck;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);

        PluginCommand cmd = getCommand("scan");
        ScanCommand scanCommand = new ScanCommand();
        cmd.setExecutor(scanCommand);
        cmd.setTabCompleter(scanCommand);

        getServer().getPluginManager().registerEvents(new JoinListener(dataManager), this);
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