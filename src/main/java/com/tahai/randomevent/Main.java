package com.tahai.randomevent;

import com.tahai.randomevent.EventTaskManager;
import com.tahai.randomevent.OneBotClient;
import com.tahai.randomevent.PlayerListener;
import com.tahai.randomevent.RandomEventCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;

public class Main extends JavaPlugin {

    private static Main instance;
    private OneBotClient oneBotClient;

    public static Main get() {
        return instance;
    }

    public OneBotClient getOneBotClient() {
        return oneBotClient;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ConfigManager.reload(getConfig());

        PluginCommand cmd = getCommand("revent");
        if (cmd != null) {
            RandomEventCommand executor = new RandomEventCommand();
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        EventTaskManager eventTaskManager = new EventTaskManager();
        eventTaskManager.start(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(eventTaskManager), this);

        if (ConfigManager.WsUrl != null && !ConfigManager.WsUrl.isEmpty()) {
            oneBotClient = new OneBotClient(URI.create(ConfigManager.WsUrl));
            oneBotClient.connect();
        }

        getLogger().info("RandomEvent 已启动");
    }

    @Override
    public void onDisable() {
        if (oneBotClient != null) {
            oneBotClient.close();
        }
        getLogger().info("RandomEvent 已关闭");
    }
}