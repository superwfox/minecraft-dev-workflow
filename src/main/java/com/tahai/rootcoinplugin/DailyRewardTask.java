package com.tahai.rootcoinplugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class DailyRewardTask extends BukkitRunnable {

    private final DataManager dataManager;

    public DailyRewardTask(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (dataManager.getQQ(player.getUniqueId()) != null) {
                dataManager.addBalance(player.getUniqueId(), 10.0);
            }
        }
        dataManager.save();
    }
}