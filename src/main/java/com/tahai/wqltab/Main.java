package com.tahai.wqltab;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;

public class Main extends JavaPlugin {

    private ConfigManager configManager;
    private DisplayTask displayTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            configManager = new ConfigManager(this);
        } catch (Exception e) {
            getLogger().severe("Failed to initialize config: " + e.getMessage());
            setEnabled(false);
            return;
        }
        PluginCommand cmd = getCommand("wqltab");
        if (cmd == null) {
            getLogger().severe("Command 'wqltab' is not defined in plugin.yml");
            setEnabled(false);
            return;
        }
        displayTask = new DisplayTask(configManager);
        WqlTabCommand executor = new WqlTabCommand(configManager, this, displayTask);
        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        displayTask.runTaskTimer(this, 0L, 20L);
        getLogger().info("WqlTab has been enabled.");
    }

    @Override
    public void onDisable() {
        if (displayTask != null) {
            displayTask.cancel();
        }
        if (configManager != null) {
            configManager.save();
            configManager.shutdown();
        }
        getLogger().info("WqlTab has been disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}