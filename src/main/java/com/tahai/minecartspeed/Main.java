package com.tahai.minecartspeed;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private SelectionManager selectionManager;
    private RegionManager regionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        selectionManager = new SelectionManager();
        regionManager = new RegionManager(getDataFolder());

        SetSpeedCommand setSpeedCommand = new SetSpeedCommand(selectionManager, regionManager);
        getCommand("minecartspeed").setExecutor(setSpeedCommand);
        getCommand("minecartspeed").setTabCompleter(setSpeedCommand);

        getServer().getPluginManager().registerEvents(new StickSelectListener(selectionManager), this);
        getServer().getPluginManager().registerEvents(new MinecartMoveListener(regionManager), this);
        getServer().getPluginManager().registerEvents(new MinecartCollisionListener(regionManager), this);
    }

    @Override
    public void onDisable() {
        if (regionManager != null) {
            regionManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }
}