package com.tahai.itemban;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        dataManager = new DataManager();
        // 注册命令
        ItembanCommand itembanCommand = new ItembanCommand();
        getCommand("itemban").setExecutor(itembanCommand);
        getCommand("itemban").setTabCompleter(itembanCommand);
        // 注册监听器
        getServer().getPluginManager().registerEvents(new BannedItemListener(), this);
        getLogger().info("ItemBan enabled.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.shutdown();
        }
        getLogger().info("ItemBan disabled.");
    }
}