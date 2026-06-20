package com.tahai.ngpumpkinpie;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataManager = new DataManager();

        PluginCommand ngCommand = getCommand("ng");
        if (ngCommand != null) {
            NgCommand executor = new NgCommand();
            ngCommand.setExecutor(executor);
            ngCommand.setTabCompleter(executor);
        }

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PieUseListener(), this);
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