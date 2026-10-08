package com.tahai.maceshieldbreak;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private WhitelistManager whitelistManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.whitelistManager = new WhitelistManager(this);

        MaceCommand maceCommand = new MaceCommand(this.whitelistManager);
        PluginCommand command = getCommand("mace");
        if (command != null) {
            command.setExecutor(maceCommand);
            command.setTabCompleter(maceCommand);
        }

        Listener shieldBlockListener = new ShieldBlockListener(this.whitelistManager);
        getServer().getPluginManager().registerEvents(shieldBlockListener, this);
    }

    @Override
    public void onDisable() {
        if (this.whitelistManager != null) {
            this.whitelistManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public WhitelistManager getWhitelistManager() {
        return this.whitelistManager;
    }
}