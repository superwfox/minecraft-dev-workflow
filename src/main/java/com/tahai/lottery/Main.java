package com.tahai.lottery;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataManager = new DataManager(this);

        LottoCommand lottoCommand = new LottoCommand();
        getCommand("lotto").setExecutor(lottoCommand);
        getCommand("lotto").setTabCompleter(lottoCommand);

        getServer().getPluginManager().registerEvents(new GUIListener(dataManager), this);
        getServer().getPluginManager().registerEvents(new KeyUseListener(), this);
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