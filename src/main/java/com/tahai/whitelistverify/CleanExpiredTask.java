package com.tahai.whitelistverify;

import org.bukkit.scheduler.BukkitRunnable;

public class CleanExpiredTask extends BukkitRunnable {

    @Override
    public void run() {
        DataManager.cleanExpired();
    }
}