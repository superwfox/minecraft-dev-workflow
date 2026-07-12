package com.tahai.customhud;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateTask extends BukkitRunnable {

    private final HUDManager hudManager;

    public UpdateTask(HUDManager hudManager) {
        this.hudManager = hudManager;
    }

    @Override
    public void run() {
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            hudManager.applyToPlayer(player);
        }
    }
}