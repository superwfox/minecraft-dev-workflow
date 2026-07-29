package com.tahai.wqltab;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

import me.clip.placeholderapi.PlaceholderAPI;

public class ScoreboardManager {

    private final Plugin plugin;

    public ScoreboardManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void updateScoreboard(Player player) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("scoreboard.enabled", false)) return;

        String title = config.getString("scoreboard.title", "");
        List<String> lines = config.getStringList("scoreboard.lines");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            title = PlaceholderAPI.setPlaceholders(player, title);
        }

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("wqltab", "dummy", ChatColor.translateAlternateColorCodes('&', title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null) continue;
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }
            line = ChatColor.translateAlternateColorCodes('&', line);
            int score = lines.size() - i;
            obj.getScore(line).setScore(score);
        }

        player.setScoreboard(board);
    }

    public void setTabList(Player player) {
        FileConfiguration config = plugin.getConfig();
        String header = config.getString("tablist.header", "");
        String footer = config.getString("tablist.footer", "");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            header = PlaceholderAPI.setPlaceholders(player, header);
            footer = PlaceholderAPI.setPlaceholders(player, footer);
        }

        header = ChatColor.translateAlternateColorCodes('&', header);
        footer = ChatColor.translateAlternateColorCodes('&', footer);

        player.setPlayerListHeaderFooter(header, footer);
    }
}