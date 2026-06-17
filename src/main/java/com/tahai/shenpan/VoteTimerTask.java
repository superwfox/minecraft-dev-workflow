package com.tahai.shenpan;

import org.bukkit.scheduler.BukkitRunnable;

public class VoteTimerTask extends BukkitRunnable {

    private final VoteManager voteManager;

    public VoteTimerTask(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public void run() {
        voteManager.endVote();
    }
}