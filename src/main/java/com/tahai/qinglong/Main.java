package com.tahai.qinglong;

import com.tahai.qinglong.command.QinglongCommand;
import com.tahai.qinglong.listener.CombatListener;
import com.tahai.qinglong.listener.GuiListener;
import com.tahai.qinglong.manager.ConfigManager;
import com.tahai.qinglong.manager.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    private ConfigManager configManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);

        getCommand("qinglong").setExecutor(new QinglongCommand());
        getCommand("qinglong").setTabCompleter(new QinglongCommand());

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(dataManager, configManager), this);

        Bukkit.getOnlinePlayers().forEach(player -> dataManager.loadPlayer(player));
    }

    @Override
    public void onDisable() {
        dataManager.saveAll();
        getServer().getScheduler().cancelTasks(this);
        instance = null;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public static ConfigManager getConfigManagerStatic() {
        return instance != null ? instance.configManager : null;
    }

    public static DataManager getDataManagerStatic() {
        return instance != null ? instance.dataManager : null;
    }

    public static Main getInstance() {
        return instance;
    }
}