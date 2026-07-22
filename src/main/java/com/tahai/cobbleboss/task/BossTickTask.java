package com.tahai.cobbleboss.task;

import com.tahai.cobbleboss.manager.BossManager;

public class BossTickTask extends org.bukkit.scheduler.BukkitRunnable {
    private final BossManager bossManager;

    public BossTickTask(BossManager bossManager) {
        this.bossManager = bossManager;
    }

    @Override
    public void run() {
        bossManager.tick();
    }
}