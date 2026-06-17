package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private VoteManager voteManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        voteManager = new VoteManager();

        getCommand("shenpan").setExecutor(new ShenPanCommand(voteManager));
        getCommand("shenpan").setTabCompleter(new ShenPanCommand(voteManager));
        getCommand("vote").setExecutor(new VoteCommand(voteManager));
        getCommand("vote").setTabCompleter(new VoteCommand(voteManager));

        // 启动一次性投票定时任务
        int duration = getConfig().getInt("vote-duration", 30);
        new VoteTimerTask(voteManager).runTaskLater(this, duration * 20L);
    }

    @Override
    public void onDisable() {
        if (voteManager != null && voteManager.isVoting()) {
            voteManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }
}