package com.tahai.weaponskills;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin {

    private DataManager dataManager;
    private Map<UUID, PlayerData> playerDataMap;

    @Override
    public void onEnable() {
        playerDataMap = new HashMap<>();

        dataManager = new DataManager(this);

        WeaponSkillsCommand weaponSkillsCommand = new WeaponSkillsCommand(dataManager);
        getCommand("weaponskill").setExecutor(weaponSkillsCommand);
        getCommand("weaponskill").setTabCompleter(weaponSkillsCommand);

        getServer().getPluginManager().registerEvents(new WeaponSkillListener(dataManager), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
            dataManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public static Main getPlugin() {
        return (Main) Bukkit.getPluginManager().getPlugin("WeaponSkills");
    }
}