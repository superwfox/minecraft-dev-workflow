package com.tahai.qqwhitelist;

import org.bukkit.plugin.java.JavaPlugin;
import java.net.URI;

public class Main extends JavaPlugin {
    private VerificationManager verificationManager;
    private OneBotClient oneBotClient;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager.init(this);

        verificationManager = new VerificationManager();

        try {
            oneBotClient = new OneBotClient(new URI(ConfigManager.WsUrl));
            oneBotClient.connect();
        } catch (Exception e) {
            getLogger().warning("无法连接 OneBot: " + e.getMessage());
        }

        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        new CleanupTask().runTaskTimer(this, 0L, 1200L);
    }

    @Override
    public void onDisable() {
        if (oneBotClient != null) {
            oneBotClient.close();
        }
        if (verificationManager != null) {
            verificationManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }
}