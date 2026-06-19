package com.tahai.backpackban;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private StateManager stateManager;

    @Override
    public void onEnable() {
        stateManager = new StateManager();

        BackpackBanCommand cmdExecutor = new BackpackBanCommand(stateManager);
        PluginCommand cmd = getCommand("backpackban");
        cmd.setExecutor(cmdExecutor);
        cmd.setTabCompleter(cmdExecutor);

        BackpackListener listener = new BackpackListener(stateManager);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public StateManager getStateManager() {
        return stateManager;
    }
}