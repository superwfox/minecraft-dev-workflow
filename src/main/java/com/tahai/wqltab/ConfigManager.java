package com.tahai.wqltab;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final Plugin plugin;
    private List<String> header;
    private List<String> footer;
    private String scoreboardTitle;
    private List<String> scoreboardLines;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        // Header
        List<String> rawHeader = config.getStringList("tab.header");
        header = new ArrayList<>();
        for (String line : rawHeader) {
            header.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        // Footer
        List<String> rawFooter = config.getStringList("tab.footer");
        footer = new ArrayList<>();
        for (String line : rawFooter) {
            footer.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        // Scoreboard title
        String rawTitle = config.getString("scoreboard.title", "");
        scoreboardTitle = ChatColor.translateAlternateColorCodes('&', rawTitle);

        // Scoreboard lines
        List<String> rawLines = config.getStringList("scoreboard.lines");
        scoreboardLines = new ArrayList<>();
        for (String line : rawLines) {
            scoreboardLines.add(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    public void reloadConfig() {
        try {
            plugin.reloadConfig();
            loadConfig();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reload config", e);
        }
    }

    public List<String> getHeader() {
        return header;
    }

    public List<String> getFooter() {
        return footer;
    }

    public String getScoreboardTitle() {
        return scoreboardTitle;
    }

    public List<String> getScoreboardLines() {
        return scoreboardLines;
    }

    public void save() {
        // No mutable data to save, placeholder for Main.onDisable
    }

    public void shutdown() {
        // Cleanup if needed
    }
}