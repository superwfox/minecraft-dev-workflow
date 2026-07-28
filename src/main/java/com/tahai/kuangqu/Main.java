package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;
    private SelectionManager selectionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataManager = new DataManager();
        selectionManager = new SelectionManager();

        // 注册命令
        KqCommand kqCommand = new KqCommand();
        getCommand("kq").setExecutor(kqCommand);
        getCommand("kq").setTabCompleter(kqCommand);

        // 注册事件监听
        getServer().getPluginManager().registerEvents(new SelectionListener(), this);

        // 启动定时任务
        new AreaCheckTask(dataManager).runTaskTimer(this, 20L, 100L);
        new DiamondDropTask(dataManager).runTaskTimer(this, 200L, 1200L);
        new ResetTask().runTaskTimer(this, 100L, 1200L);

        // 若 PlaceholderAPI 存在则注册扩展
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KuangquPlaceholderExpansion().register();
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        dataManager.saveConfig();
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
}