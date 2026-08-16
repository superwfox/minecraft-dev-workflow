package com.tahai.slimeboss;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        SpawnSlimeKingCommand command = new SpawnSlimeKingCommand();
        getCommand("spawnslimeking").setExecutor(command);
        getCommand("spawnslimeking").setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}