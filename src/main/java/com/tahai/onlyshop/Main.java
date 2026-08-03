package com.tahai.onlyshop;

import com.tahai.onlyshop.AdminCommand;
import com.tahai.onlyshop.DataManager;
import com.tahai.onlyshop.GUIListener;
import com.tahai.onlyshop.ShopCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.dataManager = new DataManager(this);

        ShopCommand shopCommand = new ShopCommand();
        getCommand("onlyshop").setExecutor(shopCommand);
        getCommand("onlyshop").setTabCompleter(shopCommand);

        AdminCommand adminCommand = new AdminCommand(dataManager);
        getCommand("shopadmin").setExecutor(adminCommand);
        getCommand("shopadmin").setTabCompleter(adminCommand);

        getServer().getPluginManager().registerEvents(new GUIListener(dataManager), this);
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
}