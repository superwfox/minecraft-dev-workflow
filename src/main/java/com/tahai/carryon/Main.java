package com.tahai.carryon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private CarryManager carryManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        carryManager = new CarryManager(this);

        CarryListener listener = new CarryListener();
        listener.init(this, carryManager);
        Bukkit.getPluginManager().registerEvents(listener, this);

        getLogger().info("[CarryOn] 已启用");
    }

    @Override
    public void onDisable() {
        if (carryManager != null) {
            carryManager.save();
            carryManager.shutdown();
        }
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("[CarryOn] 已禁用");
    }

    public static Main getInstance() {
        return instance;
    }

    public CarryManager getCarryManager() {
        return carryManager;
    }

    public static CarryManager getCarryManager(Plugin plugin) {
        Plugin carryOn = Bukkit.getPluginManager().getPlugin("CarryOn");
        if (carryOn != null && carryOn.equals(plugin)) {
            return getInstance().getCarryManager();
        }
        return null;
    }
}