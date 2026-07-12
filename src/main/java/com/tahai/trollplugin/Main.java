package com.tahai.trollplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private PrankManager prankManager;
    public static PrankManager staticPrankManager;

    @Override
    public void onEnable() {
        prankManager = new PrankManager(Bukkit.getPluginManager().getPlugin("TrollPlugin"));
        staticPrankManager = prankManager;

        TrollCommand trollCmd = new TrollCommand();
        PluginCommand cmd = getCommand("troll");
        if (cmd != null) {
            cmd.setExecutor(trollCmd);
            cmd.setTabCompleter(trollCmd);
        }

        getServer().getPluginManager().registerEvents(new ReverseMoveListener(), this);
    }

    @Override
    public void onDisable() {
        if (prankManager != null) {
            prankManager.stopAllPranks();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public PrankManager getPrankManager() {
        return prankManager;
    }
}