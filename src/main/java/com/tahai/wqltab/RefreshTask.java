package com.tahai.wqltab;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RefreshTask extends BukkitRunnable {

    private final ScoreboardManager manager;

    public RefreshTask(ScoreboardManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.updateScoreboard(player);
            manager.setTabList(player);
        }
    }
}