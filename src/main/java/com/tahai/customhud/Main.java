package com.tahai.customhud;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private HUDManager hudManager;
    private ResourcePackService resourcePackService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        hudManager = new HUDManager(this);
        resourcePackService = new ResourcePackService();

        getCommand("customhud").setExecutor(new CustomHUDCommand());
        getCommand("customhud").setTabCompleter(new CustomHUDCommand());

        Bukkit.getPluginManager().registerEvents(new QuitListener(), this);

        new UpdateTask(hudManager).runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        hudManager.shutdown();
        getServer().getScheduler().cancelTasks(this);
    }

    public HUDManager getHUDManager() {
        return hudManager;
    }

    public ResourcePackService getResourcePackService() {
        return resourcePackService;
    }
}