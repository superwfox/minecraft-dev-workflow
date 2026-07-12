package com.tahai.boatlandenhancer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private SpeedConfigManager speedConfigManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        speedConfigManager = new SpeedConfigManager(this);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("BoatLandEnhancer");
        Bukkit.getPluginManager().registerEvents(new VehicleMoveListener(plugin), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractEntityListener(plugin), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public SpeedConfigManager getSpeedConfigManager() {
        return speedConfigManager;
    }
}