package com.tahai.voteskipnight;

import org.bukkit.scheduler.BukkitRunnable;

public class VoteTimeoutTask extends BukkitRunnable {
    private final VoteManager voteManager;

    public VoteTimeoutTask(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public void run() {
        voteManager.handleTimeout();
    }
}