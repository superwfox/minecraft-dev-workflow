package com.tahai.infinitewarehouse;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataManager = new DataManager(this);

        WarehouseCommand warehouseCmd = new WarehouseCommand();
        getCommand("warehouse").setExecutor(warehouseCmd);
        getCommand("warehouse").setTabCompleter(warehouseCmd);

        ReloadCommand reloadCmd = new ReloadCommand();
        getCommand("warereload").setExecutor(reloadCmd);
        getCommand("warereload").setTabCompleter(reloadCmd);

        getServer().getPluginManager().registerEvents(new RightClickListener(), this);
        getServer().getPluginManager().registerEvents(new WarehouseClickListener(dataManager), this);
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