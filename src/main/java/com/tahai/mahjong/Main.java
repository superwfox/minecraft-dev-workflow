package com.tahai.mahjong;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private PlayerDataManager playerDataManager;
    private GameManager gameManager;

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        playerDataManager = new PlayerDataManager(this);
        gameManager = new GameManager(playerDataManager);

        MjCommand mjCommand = new MjCommand(gameManager);
        getCommand("mj").setExecutor(mjCommand);
        getCommand("mj").setTabCompleter(mjCommand);

        getServer().getPluginManager().registerEvents(new TableInteractListener(gameManager), this);

        Bukkit.getScheduler().runTaskTimer(this, new GameTickTask(gameManager), 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.save();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }
}