package com.tahai.jiyueserverplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager();
        configManager.init(this);
        new ScoreboardTask().runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (configManager != null) {
            configManager.save();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}