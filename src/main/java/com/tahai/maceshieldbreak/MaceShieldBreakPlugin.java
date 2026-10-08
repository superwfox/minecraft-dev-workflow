package com.tahai.maceshieldbreak;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MaceShieldBreakPlugin extends JavaPlugin {

    private MaceWhitelistManager whitelistManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File whitelistFile = new File(getDataFolder(), "whitelist.yml");
        if (!whitelistFile.exists()) {
            saveResource("whitelist.yml", false);
        }

        whitelistManager = new MaceWhitelistManager(this);

        PluginCommand command = getCommand("macewhitelist");
        if (command != null) {
            MaceWhitelistCommand executor = new MaceWhitelistCommand(whitelistManager);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getServer().getPluginManager().registerEvents(new ShieldBlockListener(whitelistManager), this);
    }

    @Override
    public void onDisable() {
        if (whitelistManager != null) {
            whitelistManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public MaceWhitelistManager getMaceWhitelistManager() {
        return whitelistManager;
    }
}