package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class VoteManager {

    private final Plugin plugin;
    private final VoteDataManager dataManager;
    private final Map<String, String> messages = new HashMap<>();

    private boolean active = false;
    private UUID candidate1;
    private UUID candidate2;
    private final Map<UUID, UUID> voterChoices = new HashMap<>();
    private int taskId = -1;

    public VoteManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataManager = new VoteDataManager();
        loadMessages();
    }

    private void loadMessages() {
        File msgFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(msgFile);
        for (String key : config.getKeys(false)) {
            messages.put(key, config.getString(key, ""));
        }
    }

    public boolean startVote(UUID candidate1, UUID candidate2) {
        if (active) {
            cancelCurrentVote();
        }
        this.candidate1 = candidate1;
        this.candidate2 = candidate2;
        voterChoices.clear();
        active = true;

        int duration = plugin.getConfig().getInt("vote-duration-seconds", 60);
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                endVote();
            }
        }.runTaskLater(plugin, duration * 20L).getTaskId();

        broadcast(messages.getOrDefault("vote-start", "§6Vote started!"));
        return true;
    }

    public boolean vote(UUID voter, UUID target) {
        if (!active) return false;
        if (!target.equals(candidate1) && !target.equals(candidate2)) return false;
        voterChoices.put(voter, target);
        broadcast(messages.getOrDefault("vote-success", "§aVote recorded!"));
        return true;
    }

    public void cancelCurrentVote() {
        if (!active) return;
        active = false;
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        voterChoices.clear();
    }

    private void endVote() {
        active = false;
        taskId = -1;

        int votes1 = 0, votes2 = 0;
        for (UUID choice : voterChoices.values()) {
            if (choice.equals(candidate1)) votes1++;
            else if (choice.equals(candidate2)) votes2++;
        }

        UUID loser = null;
        String resultMessage;
        if (votes1 > votes2) {
            loser = candidate1;
            resultMessage = messages.getOrDefault("result-broadcast", "§c%player% has been voted!").replace("%player%", Bukkit.getOfflinePlayer(loser).getName());
        } else if (votes2 > votes1) {
            loser = candidate2;
            resultMessage = messages.getOrDefault("result-broadcast", "§c%player% has been voted!").replace("%player%", Bukkit.getOfflinePlayer(loser).getName());
        } else {
            resultMessage = messages.getOrDefault("tie", "§eIt's a tie! No one is punished.");
            broadcast(resultMessage);
            saveResult(null, votes1, votes2);
            return;
        }

        if (loser != null) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(loser);
            if (player != null) {
                player.setHealth(0);
            }
        }
        broadcast(resultMessage);
        saveResult(loser, votes1, votes2);
    }

    private void saveResult(UUID loser, int votes1, int votes2) {
        VoteDataManager.VoteResult result = new VoteDataManager.VoteResult(
                Arrays.asList(candidate1, candidate2),
                Map.of(candidate1, votes1, candidate2, votes2),
                loser,
                System.currentTimeMillis()
        );
        dataManager.appendResult(result);
    }

    private void broadcast(String message) {
        Bukkit.broadcastMessage(message);
    }

    public boolean isActive() {
        return active;
    }
}