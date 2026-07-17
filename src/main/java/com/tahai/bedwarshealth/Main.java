package com.tahai.bedwarshealth;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new GameListener(), this);
        getLogger().info("BedWarsHealth enabled");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("BedWarsHealth disabled");
    }
}