package com.tahai.qqgroupsync;

import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;

public class Main extends JavaPlugin {

    private OneBotClient oneBotClient;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager.load(getConfig());

        oneBotClient = new OneBotClient(URI.create(ConfigManager.WsUrl));
        oneBotClient.connect();

        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);

        getLogger().info("QQGroupSync has been enabled.");
    }

    @Override
    public void onDisable() {
        if (oneBotClient != null) {
            oneBotClient.close();
        }
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("QQGroupSync has been disabled.");
    }

    public OneBotClient getOneBotClient() {
        return oneBotClient;
    }
}