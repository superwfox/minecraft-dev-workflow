package com.tahai.wqltab;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.List;

public class DisplayTask extends BukkitRunnable {

    private final ConfigManager configManager;
    private Economy economy;

    public DisplayTask(ConfigManager configManager) {
        this.configManager = configManager;
        setupEconomy();
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        }
    }

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WqlTab");
        if (plugin == null || !plugin.isEnabled()) return;

        configManager.reloadConfig();

        List<String> headerLines = configManager.getHeader();
        List<String> footerLines = configManager.getFooter();
        String title = configManager.getScoreboardTitle();
        List<String> scoreboardLines = configManager.getScoreboardLines();

        String header = String.join("\n", headerLines);
        String footer = String.join("\n", footerLines);

        for (Player player : Bukkit.getOnlinePlayers()) {
            String parsedHeader = parsePlaceholders(player, header);
            String parsedFooter = parsePlaceholders(player, footer);
            String parsedTitle = parsePlaceholders(player, title);
            List<String> parsedLines = scoreboardLines.stream()
                    .map(line -> parsePlaceholders(player, line))
                    .toList();

            Component headerComponent = LegacyComponentSerializer.legacySection().deserialize(parsedHeader);
            Component footerComponent = LegacyComponentSerializer.legacySection().deserialize(parsedFooter);
            player.setPlayerListHeaderFooter(headerComponent, footerComponent);

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();
            Objective obj = board.registerNewObjective("tab", "dummy", parsedTitle);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            int score = parsedLines.size();
            for (String line : parsedLines) {
                if (line.isEmpty()) continue;
                obj.getScore(line).setScore(score--);
                if (score <= 0) break;
            }

            player.setScoreboard(board);
        }
    }

    private String parsePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) return text;
        String result = ChatColor.translateAlternateColorCodes('&', text);

        Plugin placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderAPI != null && placeholderAPI.isEnabled()) {
            // In API versions targeting 1.21, PlaceholderAPI.setPlaceholders returns Component instead of String.
            Component component = PlaceholderAPI.setPlaceholders(player, result);
            if (component != null) {
                result = LegacyComponentSerializer.legacySection().serialize(component);
            }
        } else {
            if (economy != null) {
                result = result.replace("%vault_eco_balance%", economy.format(economy.getBalance(player)));
            } else {
                result = result.replace("%vault_eco_balance%", "");
            }
        }
        return result;
    }
}