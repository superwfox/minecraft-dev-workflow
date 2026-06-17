package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class VoteManager {

    private final Plugin plugin;
    private final Map<UUID, String> voterMap = new HashMap<>();
    private final Map<String, Integer> voteCount = new HashMap<>();
    private final Set<String> candidates = new HashSet<>();
    private boolean votingActive = false;

    private String logFilePath;
    private String killCommand;
    private boolean requireMajority;

    public VoteManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        logFilePath = config.getString("vote.log-file", "vote_log.txt");
        killCommand = config.getString("vote.kill-command", "kill %player%");
        requireMajority = config.getBoolean("vote.require-majority", false);
    }

    public boolean createVote(Collection<String> candidateNames) {
        if (votingActive || candidateNames == null || candidateNames.size() < 2) {
            return false;
        }
        voterMap.clear();
        voteCount.clear();
        candidates.clear();

        candidates.addAll(candidateNames);
        for (String name : candidates) {
            voteCount.put(name, 0);
        }
        votingActive = true;
        return true;
    }

    public boolean isVoting() {
        return votingActive;
    }

    public boolean vote(Player player, String candidate) {
        if (!votingActive || !candidates.contains(candidate)) {
            return false;
        }
        UUID uuid = player.getUniqueId();
        String previous = voterMap.get(uuid);
        if (previous != null) {
            voteCount.merge(previous, -1, Integer::sum);
        }
        voteCount.merge(candidate, 1, Integer::sum);
        voterMap.put(uuid, candidate);
        return true;
    }

    public String endVote() {
        if (!votingActive) {
            return null;
        }
        votingActive = false;

        String winner = null;
        int maxVotes = -1;
        int totalVotes = voterMap.size();
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            int votes = entry.getValue();
            if (votes > maxVotes) {
                maxVotes = votes;
                winner = entry.getKey();
            } else if (votes == maxVotes) {
                winner = null;
            }
        }
        if (winner != null && requireMajority && maxVotes <= totalVotes / 2) {
            winner = null;
        }

        if (winner != null) {
            String command = killCommand.replace("%player%", winner);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        writeLog(winner);
        return winner;
    }

    private void writeLog(String winner) {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File logFile = new File(dataFolder, logFilePath);
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println("--- Vote Result ---");
                writer.println("Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println("Candidates: " + String.join(", ", candidates));
                writer.println("Voters: " + voterMap.size());
                for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue() + " votes");
                }
                writer.println("Winner: " + (winner == null ? "None" : winner));
                writer.println();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write vote log: " + e.getMessage());
        }
    }

    public void clear() {
        voterMap.clear();
        voteCount.clear();
        candidates.clear();
        votingActive = false;
    }

    public void shutdown() {
        clear();
    }
}