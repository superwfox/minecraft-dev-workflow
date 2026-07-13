package com.tahai.randomfishingloot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomFishingLoot extends JavaPlugin {

    private static RandomFishingLoot instance;

    private ConfigManager configManager;
    private LootManager lootManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        lootManager = new LootManager(configManager);

        RfishingCommand rfishingCommand = new RfishingCommand();
        getCommand("rfishing").setExecutor(rfishingCommand);
        getCommand("rfishing").setTabCompleter(rfishingCommand);

        FishingListener fishingListener = new FishingListener();
        Bukkit.getPluginManager().registerEvents(fishingListener, this);
    }

    @Override
    public void onDisable() {
        // 没有需要保存的数据或取消的任务
    }

    public static RandomFishingLoot getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }
}