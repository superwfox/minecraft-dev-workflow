package com.tahai.hh;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;
    private ExchangeMenuGUI exchangeMenuGUI;

    @Override
    public void onEnable() {
        // 实例化服务
        this.dataManager = new DataManager(this);
        this.exchangeMenuGUI = new ExchangeMenuGUI(this);

        // 注册 /hh 命令
        PluginCommand hhCommand = getCommand("hh");
        if (hhCommand != null) {
            OpenMenuCommand executor = new OpenMenuCommand();
            hhCommand.setExecutor(executor);
            hhCommand.setTabCompleter(executor);
        }

        // 注册监听器
        getServer().getPluginManager().registerEvents(new MenuClickListener(this.dataManager), this);
        getServer().getPluginManager().registerEvents(this.exchangeMenuGUI, this);

        getLogger().info("hh 插件已启用");
    }

    @Override
    public void onDisable() {
        if (this.dataManager != null) {
            this.dataManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("hh 插件已禁用");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ExchangeMenuGUI getExchangeMenuGUI() {
        return exchangeMenuGUI;
    }
}