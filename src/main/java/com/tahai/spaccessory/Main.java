package com.tahai.spaccessory;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private PermissionManager permissionManager;

    @Override
    public void onEnable() {
        permissionManager = new PermissionManager(this);

        getCommand("sp").setExecutor(new SpCommand());
        getCommand("sp").setTabCompleter(new SpCommand());

        getServer().getPluginManager().registerEvents(new GuiClickListener(this, permissionManager), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}