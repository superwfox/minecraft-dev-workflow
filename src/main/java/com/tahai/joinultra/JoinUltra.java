package com.tahai.joinultra;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinUltra extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        JoinUltraCommand command = new JoinUltraCommand();
        PluginCommand joinultraCommand = getCommand("joinultra");
        joinultraCommand.setExecutor(command);
        joinultraCommand.setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (configManager != null) {
            configManager.save();
            configManager.shutdown();
        }
        for (BossBar bossBar : Bukkit.getBossBars()) {
            bossBar.removeAll();
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}