package com.tahai.maceshieldbreaker;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private WhitelistManager whitelistManager;

    @Override
    public void onEnable() {
        whitelistManager = new WhitelistManager(this);
        whitelistManager.load();

        PluginCommand command = getCommand("macewhitelist");
        if (command != null) {
            MaceWhitelistCommand executor = new MaceWhitelistCommand();
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getServer().getPluginManager().registerEvents(new ShieldBlockListener(), this);
    }

    @Override
    public void onDisable() {
        if (whitelistManager != null) {
            whitelistManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }
}