package com.tahai.baoshi;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private DataManager dataManager;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        dataManager = new DataManager(this);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Baoshi");

        GiveCommand giveCommand = new GiveCommand();
        getCommand("baoshi").setExecutor(giveCommand);
        getCommand("baoshi").setTabCompleter(giveCommand);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new InventoryClickListener(dataManager), this);
        pm.registerEvents(new ArmorEquipListener(dataManager, plugin), this);
        pm.registerEvents(new DamageListener(), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}