package com.tahai.authwebmanager;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private AuthMeIntegration authMeIntegration;
    private ModManager modManager;
    private PlayerDataManager playerDataManager;
    private WebServerManager webServerManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        authMeIntegration = new AuthMeIntegration();
        modManager = new ModManager();
        playerDataManager = new PlayerDataManager();
        webServerManager = new WebServerManager();
    }

    @Override
    public void onDisable() {
        if (modManager != null) {
            modManager.save();
            modManager.shutdown();
        }

        if (webServerManager != null) {
            webServerManager.save();
            webServerManager.shutdown();
        }

        getServer().getScheduler().cancelTasks(this);
    }

    public AuthMeIntegration getAuthMeIntegration() {
        return authMeIntegration;
    }

    public ModManager getModManager() {
        return modManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public WebServerManager getWebServerManager() {
        return webServerManager;
    }
}