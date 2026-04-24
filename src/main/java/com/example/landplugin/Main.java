package com.example.landplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private LandManager landManager;

    @Override
    public void onEnable() {
        landManager = new LandManager();
        landManager.loadLands();
        Bukkit.getPluginManager().registerEvents(new EventListener(landManager), this);
        getCommand("land").setExecutor(new LandCommand(this));
    }

    @Override
    public void onDisable() {
        if (landManager != null) {
            landManager.saveLands();
        }
    }

    public LandManager getLandManager() {
        return landManager;
    }
}