package com.tahai.baoshi;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // 保存默认配置文件（如果不存在）
        saveDefaultConfig();
        saveResource("messages.yml", false);

        // 加载消息配置并注入到命令类
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (messagesFile.exists()) {
            YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            GemCommand.initMessages(messagesConfig);
        }

        // 注册命令
        GemCommand gemCommand = new GemCommand();
        getCommand("gem").setExecutor(gemCommand);
        getCommand("gem").setTabCompleter(gemCommand);

        // 注册监听器
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new AttributeListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}