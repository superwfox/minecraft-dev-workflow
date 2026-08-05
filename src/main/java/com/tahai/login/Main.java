package com.tahai.login;

import com.tahai.authlogin.DataManager;
import com.tahai.authlogin.FreezeListener;
import com.tahai.authlogin.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        dataManager = new DataManager(getDataFolder());
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(dataManager), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
            dataManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }
}