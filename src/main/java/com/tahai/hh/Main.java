package com.tahai.hh;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // 注册命令
        HhCommand hhCommand = new HhCommand();
        getCommand("hh").setExecutor(hhCommand);
        getCommand("hh").setTabCompleter(hhCommand);

        // 注册事件监听
        getServer().getPluginManager().registerEvents(new GuiClickListener(), this);
    }

    @Override
    public void onDisable() {
        // 无需处理
    }
}