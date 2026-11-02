package com.tahai.freezecontrol;

import com.tahai.freezecontrol.freeze.FreezeCommand;
import com.tahai.freezecontrol.freeze.FreezeListener;
import com.tahai.freezecontrol.freeze.FreezeManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private FreezeManager freezeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        freezeManager = new FreezeManager(this);

        FreezeCommand freezeCommand = new FreezeCommand(this, freezeManager);
        PluginCommand command = getCommand("freeze");
        if (command != null) {
            command.setExecutor(freezeCommand);
            command.setTabCompleter(freezeCommand);
        }

        FreezeListener freezeListener = new FreezeListener(freezeManager);
        getServer().getPluginManager().registerEvents(freezeListener, this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}