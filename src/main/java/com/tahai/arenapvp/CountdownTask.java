package com.tahai.arenapvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class CountdownTask extends BukkitRunnable {

    private int secondsLeft;
    private final ArenaManager.Game game;
    private final Runnable onFinish;

    public CountdownTask(int seconds, ArenaManager.Game game, Runnable onFinish) {
        this.secondsLeft = seconds;
        this.game = game;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        if (secondsLeft > 0) {
            String msg = ChatColor.YELLOW + Integer.toString(secondsLeft);
            if (game != null) {
                for (UUID uuid : game.getPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage(msg);
                    }
                }
            } else {
                Bukkit.broadcastMessage(msg);
            }
            secondsLeft--;
        } else {
            onFinish.run();
            cancel();
        }
    }
}