package com.tahai.cobblemonboss;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private BossManager bossManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("bosses.yml", false);

        bossManager = new BossManager();

        BossCommand bossCmd = new BossCommand();
        getCommand("boss").setExecutor(bossCmd);
        getCommand("boss").setTabCompleter(bossCmd);

        getServer().getPluginManager().registerEvents(new BossListener(), this);

        new BossTask().runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        bossManager.save();
        bossManager.shutdown();
        getServer().getScheduler().cancelTasks(this);
    }

    public BossManager getBossManager() {
        return bossManager;
    }
}