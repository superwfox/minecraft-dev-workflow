package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private MineManager mineManager;
    private ResetTask resetTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 实例化 MineManager 并从配置加载矿区
        mineManager = new MineManager(Bukkit.getPluginManager().getPlugin("Kuangqu"));
        ConfigurationSection minesSection = getConfig().getConfigurationSection("mines");
        if (minesSection != null) {
            for (String key : minesSection.getKeys(false)) {
                ConfigurationSection mineSection = minesSection.getConfigurationSection(key);
                if (mineSection == null) continue;
                String world = mineSection.getString("world");
                int minX = mineSection.getInt("min-x");
                int minY = mineSection.getInt("min-y");
                int minZ = mineSection.getInt("min-z");
                int maxX = mineSection.getInt("max-x");
                int maxY = mineSection.getInt("max-y");
                int maxZ = mineSection.getInt("max-z");
                String resetTime = mineSection.getString("reset-time", "0 0 * * *");
                mineManager.addMine(key, world, minX, minY, minZ, maxX, maxY, maxZ, resetTime);
            }
        }

        // 注册 /kq 命令
        KqCommand kqCommand = new KqCommand();
        kqCommand.setMineManager(mineManager);
        getCommand("kq").setExecutor(kqCommand);
        getCommand("kq").setTabCompleter(kqCommand);

        // 注册事件监听
        getServer().getPluginManager().registerEvents(new MineListener(mineManager), this);

        // 启动定时任务（每20tick = 1秒）
        resetTask = new ResetTask(mineManager);
        resetTask.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (mineManager != null) {
            mineManager.save();
            mineManager.shutdown();
        }
        if (resetTask != null) {
            resetTask.cancel();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    public MineManager getMineManager() {
        return mineManager;
    }
}