package com.tahai.jiyueserverplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class ScoreboardTask extends BukkitRunnable {

    @Override
    public void run() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard board = manager.getMainScoreboard();
        Objective objective = board.getObjective("ggl");
        if (objective == null) {
            objective = board.registerNewObjective("ggl", "dummy", "ggl");
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            org.bukkit.scoreboard.Score score = objective.getScore(player);
            score.setScore(score.getScore() + 1);
        }
    }
}