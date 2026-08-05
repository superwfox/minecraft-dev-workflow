package com.tahai.fakeplayerplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private FakePlayerManager fakePlayerManager;

    @Override
    public void onEnable() {
        this.fakePlayerManager = new FakePlayerManager();

        FakePlayerCommand fakePlayerCommand = new FakePlayerCommand();
        getCommand("fakeplayer").setExecutor(fakePlayerCommand);
        getCommand("fakeplayer").setTabCompleter(fakePlayerCommand);

        getServer().getPluginManager().registerEvents(new FakePlayerDeathListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (fakePlayerManager != null) {
            fakePlayerManager.save();
            fakePlayerManager.shutdown();
        }
    }

    public FakePlayerManager getFakePlayerManager() {
        return fakePlayerManager;
    }
}