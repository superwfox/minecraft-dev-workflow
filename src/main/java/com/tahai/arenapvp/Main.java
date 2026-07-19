package com.tahai.arenapvp;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        arenaManager = new ArenaManager(this);

        ArenaCommand arenaCommand = new ArenaCommand();
        PluginCommand arena = getCommand("arena");
        if (arena != null) {
            arena.setExecutor(arenaCommand);
            arena.setTabCompleter(arenaCommand);
        }
        PluginCommand duel = getCommand("duel");
        if (duel != null) {
            duel.setExecutor(arenaCommand);
            duel.setTabCompleter(arenaCommand);
        }

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GameListener(), this);
        pm.registerEvents(new ArenaMenuListener(arenaManager), this);

        if (pm.getPlugin("PlaceholderAPI") != null) {
            new PapiExpansion().register();
        }

        getLogger().info("ArenaPVP enabled.");
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) {
            arenaManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("ArenaPVP disabled.");
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}