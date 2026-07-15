package com.tahai.voteskipnight;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private VoteManager voteManager;

    @Override
    public void onEnable() {
        voteManager = new VoteManager();

        VoteCommand voteCommand = new VoteCommand();
        getCommand("tgzs").setExecutor(voteCommand);
        getCommand("tgzs").setTabCompleter(voteCommand);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }
}