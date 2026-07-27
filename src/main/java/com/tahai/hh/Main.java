package com.tahai.hh;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private SessionManager sessionManager;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        sessionManager = new SessionManager(this);

        HhCommand hhCommand = new HhCommand();
        getCommand("hh").setExecutor(hhCommand);
        getCommand("hh").setTabCompleter(hhCommand);

        getServer().getPluginManager().registerEvents(new ChatListener(), this);
    }

    @Override
    public void onDisable() {
        if (sessionManager != null) {
            sessionManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}