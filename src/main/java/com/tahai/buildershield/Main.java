package com.tahai.buildershield;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ConfigManager configManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager();
        dataManager = new DataManager();

        SetShieldItemCommand shieldItemCommand = new SetShieldItemCommand();
        PluginCommand command = getCommand("buildershield");
        if (command != null) {
            command.setExecutor(shieldItemCommand);
            command.setTabCompleter(shieldItemCommand);
        }

        getServer().getPluginManager().registerEvents(new ShieldListener(), this);

        new ShieldTask(configManager, dataManager).runTaskTimer(this, 0L, 10L);
    }

    @Override
    public void onDisable() {
        if (configManager != null) {
            configManager.save();
        }
        if (dataManager != null) {
            dataManager.save();
            dataManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}