package com.tahai.qqwhitelist;

import org.bukkit.scheduler.BukkitRunnable;

public class CleanupTask extends BukkitRunnable {
    @Override
    public void run() {
        VerificationManager.cleanExpired();
    }
}