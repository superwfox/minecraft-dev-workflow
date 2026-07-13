package com.tahai.randomfishingloot;

import org.bukkit.plugin.java.JavaPlugin;

public class RandomFishingLoot extends JavaPlugin {

    private ConfigManager configManager;
    private LootManager lootManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.reload();

        lootManager = new LootManager();

        RfishingCommand rfCommand = new RfishingCommand();
        rfCommand.setConfigManager(configManager);

        getCommand("rfishing").setExecutor(rfCommand);
        getCommand("rfishing").setTabCompleter(rfCommand);

        FishingListener fishingListener = new FishingListener(configManager);
        getServer().getPluginManager().registerEvents(fishingListener, this);

        getLogger().info("RandomFishingLoot has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("RandomFishingLoot has been disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }
}