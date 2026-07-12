package com.tahai.whitelistverify;

import org.bukkit.plugin.java.JavaPlugin;
import java.net.URI;

public class Main extends JavaPlugin {

    private static DataManager dataManager;
    private static OneBotClient oneBotClient;

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static OneBotClient getOneBotClient() {
        return oneBotClient;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        ConfigManager configManager = new ConfigManager();
        configManager.reload();

        dataManager = new DataManager();

        try {
            oneBotClient = new OneBotClient(URI.create(ConfigManager.WsUrl));
            oneBotClient.addHeader("Authorization", "Bearer " + ConfigManager.AccessToken);
            oneBotClient.connect();
        } catch (Exception e) {
            getLogger().warning("无法连接OneBot: " + e.getMessage());
        }

        getServer().getPluginManager().registerEvents(new WhiteListListener(), this);
        new CleanExpiredTask().runTaskTimer(this, 0L, 600L);
    }

    @Override
    public void onDisable() {
        if (oneBotClient != null) {
            oneBotClient.close();
        }
        if (dataManager != null) {
            dataManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }
}