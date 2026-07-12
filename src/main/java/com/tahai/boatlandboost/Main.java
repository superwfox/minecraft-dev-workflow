package com.tahai.boatlandboost;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new BoatMoveListener(), this);
    }

    @Override
    public void onDisable() {
        // 取消所有调度任务
        getServer().getScheduler().cancelTasks(this);
    }
}