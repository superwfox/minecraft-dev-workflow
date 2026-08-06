package com.tahai.joinultra;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BossBarRemoveTask extends BukkitRunnable {

    private final BossBar bossBar;
    private final Player player;

    public BossBarRemoveTask(BossBar bossBar, Player player) {
        this.bossBar = bossBar;
        this.player = player;
    }

    @Override
    public void run() {
        bossBar.removePlayer(player);
        if (bossBar.getPlayers().isEmpty()) {
            bossBar.removeAll();
        }
    }
}