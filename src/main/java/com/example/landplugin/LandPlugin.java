package com.example.landplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LandPlugin extends JavaPlugin {

    private LandManager landManager;
    private EventListener eventListener;

    @Override
    public void onEnable() {
        landManager = new LandManager();
        landManager.loadLands();

        eventListener = new EventListener(landManager);
        Bukkit.getPluginManager().registerEvents(eventListener, this);

        getCommand("land").setExecutor(new LandCommand(landManager));
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