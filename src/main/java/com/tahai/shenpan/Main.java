package com.tahai.shenpan;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private VoteManager voteManager;
    private VoteDataManager voteDataManager;

    @Override
    public void onEnable() {
        // 1) 复制默认配置文件
        saveDefaultConfig();
        saveResource("messages.yml", false);

        // 2) 实例化服务
        voteManager = new VoteManager(this);
        voteDataManager = new VoteDataManager();

        // 3) 注册命令 /shenpan
        ShenpanCommand cmdExecutor = new ShenpanCommand(voteManager);
        getCommand("shenpan").setExecutor(cmdExecutor);
        getCommand("shenpan").setTabCompleter(cmdExecutor);

        // 从 config 读取别名并设置
        List<String> aliases = getConfig().getStringList("aliases");
        if (aliases != null && !aliases.isEmpty()) {
            getCommand("shenpan").setAliases(aliases);
        }

        // 4) 无监听器
        // 5) 无调度任务

        getLogger().info("Shenpan 审判插件已启用");
    }

    @Override
    public void onDisable() {
        if (voteManager != null && voteManager.isActive()) {
            voteManager.cancelCurrentVote();
        }
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Shenpan 审判插件已禁用");
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }

    public VoteDataManager getVoteDataManager() {
        return voteDataManager;
    }
}