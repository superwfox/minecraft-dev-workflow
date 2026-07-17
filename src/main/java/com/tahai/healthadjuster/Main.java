package com.tahai.healthadjuster;

import com.tahai.healthadjuster.HealthAdjustListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        double health = getConfig().getDouble("health", 40);
        getServer().getPluginManager().registerEvents(new HealthAdjustListener(), this);
        getLogger().info("HealthAdjuster enabled. Configured health: " + health);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("HealthAdjuster disabled.");
    }
}