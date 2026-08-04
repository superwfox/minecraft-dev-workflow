package com.tahai.authweb;

import com.tahai.authweb.manager.AuthMeDatabaseManager;
import com.tahai.authweb.manager.HttpServerManager;
import com.tahai.authweb.manager.ModManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private AuthMeDatabaseManager authMeDatabaseManager;
    private ModManager modManager;
    private HttpServerManager httpServerManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        authMeDatabaseManager = new AuthMeDatabaseManager();
        modManager = new ModManager(this);
        httpServerManager = new HttpServerManager(authMeDatabaseManager, modManager);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        if (httpServerManager != null) {
            httpServerManager.shutdown();
        }
        if (authMeDatabaseManager != null) {
            authMeDatabaseManager.shutdown();
        }
        if (modManager != null) {
            modManager.save();
            modManager.shutdown();
        }
    }

    public AuthMeDatabaseManager getAuthMeDatabaseManager() {
        return authMeDatabaseManager;
    }

    public ModManager getModManager() {
        return modManager;
    }

    public HttpServerManager getHttpServerManager() {
        return httpServerManager;
    }
}