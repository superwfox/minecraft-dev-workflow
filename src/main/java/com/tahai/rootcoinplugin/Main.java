package com.tahai.rootcoinplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private DataManager dataManager;

    @Override
    public void onEnable() {
        dataManager = new DataManager();

        BindCommand bindCommand = new BindCommand();
        getCommand("bind").setExecutor(bindCommand);
        getCommand("bind").setTabCompleter(bindCommand);

        ShopCommand shopCommand = new ShopCommand();
        getCommand("shop").setExecutor(shopCommand);
        getCommand("shop").setTabCompleter(shopCommand);

        TradeCommand tradeCommand = new TradeCommand();
        getCommand("trade").setExecutor(tradeCommand);
        getCommand("trade").setTabCompleter(tradeCommand);

        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        new DailyRewardTask(dataManager).runTaskTimer(this, 0L, 1728000L);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}