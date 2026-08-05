package com.tahai.fakeplayer;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private FakePlayerManager fakePlayerManager;

    @Override
    public void onEnable() {
        fakePlayerManager = new FakePlayerManager();

        FakePlayerCommand commandExecutor = new FakePlayerCommand();
        PluginCommand command = getCommand("fakeplayer");
        if (command != null) {
            command.setExecutor(commandExecutor);
            command.setTabCompleter(commandExecutor);
        }

        getServer().getPluginManager().registerEvents(new FakePlayerListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public FakePlayerManager getFakePlayerManager() {
        return fakePlayerManager;
    }
}