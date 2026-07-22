package com.tahai.cobbleboss;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.tahai.cobbleboss.command.CobbleBossCommand;
import com.tahai.cobbleboss.config.ConfigManager;
import com.tahai.cobbleboss.listener.BossEventListener;
import com.tahai.cobbleboss.manager.BossManager;
import com.tahai.cobbleboss.task.BossTickTask;

public class Main extends JavaPlugin {

    private ConfigManager configManager;
    private BossManager bossManager;
    private BukkitTask tickTaskHandle;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        bossManager = new BossManager(this, configManager);
        PluginCommand command = getCommand("cobbleboss");
        if (command != null) {
            CobbleBossCommand executor = new CobbleBossCommand(configManager, bossManager);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
        getServer().getPluginManager().registerEvents(new BossEventListener(bossManager, configManager), this);
        BossTickTask tickTask = new BossTickTask(bossManager);
        tickTaskHandle = tickTask.runTaskTimer(this, 0L, 1L);
        getLogger().info("CobbleBoss has been enabled.");
    }

    @Override
    public void onDisable() {
        if (tickTaskHandle != null) {
            tickTaskHandle.cancel();
        }
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("CobbleBoss has been disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }
}