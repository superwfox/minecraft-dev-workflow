package com.tahai.loginplugin;

import com.tahai.authlogin.DataManager;
import com.tahai.authlogin.FreezeListener;
import com.tahai.authlogin.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        dataManager = new DataManager(getDataFolder());
        getServer().getPluginManager().registerEvents(new JoinListener(dataManager), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.shutdown();
        }
    }
}