package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class VoteManager {

    private final Plugin plugin;
    private boolean active = false;
    private Player initiator;
    private final Map<UUID, UUID> votes = new HashMap<>(); // voter -> candidate
    private BukkitTask countdownTask;
    private int remainingSeconds;
    private int durationSeconds;

    public VoteManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return active;
    }

    public void startVote(Player initiator, int durationSeconds) {
        if (active) {
            initiator.sendMessage(ChatColor.RED + "投票已在进行中");
            return;
        }
        this.active = true;
        this.initiator = initiator;
        this.durationSeconds = durationSeconds;
        this.remainingSeconds = durationSeconds;
        this.votes.clear();

        Bukkit.broadcastMessage(
            Messages.getString("vote-start")
                .replace("%initiator%", initiator.getName())
                .replace("%duration%", String.valueOf(durationSeconds))
        );

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                remainingSeconds--;
                if (remainingSeconds > 0) {
                    String msg = Messages.getString("countdown")
                        .replace("%seconds%", String.valueOf(remainingSeconds));
                    Bukkit.broadcastMessage(msg);
                } else {
                    endVote();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void castVote(Player voter, Player candidate) {
        if (!active) {
            voter.sendMessage(ChatColor.RED + "当前没有进行中的投票");
            return;
        }
        if (voter.equals(candidate)) {
            voter.sendMessage(ChatColor.RED + "不能投票给自己");
            return;
        }
        votes.put(voter.getUniqueId(), candidate.getUniqueId());
        voter.sendMessage(ChatColor.GREEN + "你已投票给 " + candidate.getName());
    }

    public void endVote() {
        if (!active) return;
        active = false;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        // 计算得票数
        Map<UUID, Integer> tally = new HashMap<>();
        for (UUID candidateId : votes.values()) {
            tally.merge(candidateId, 1, Integer::sum);
        }

        if (tally.isEmpty()) {
            Bukkit.broadcastMessage(Messages.getString("vote-end").replace("%result%", "无任何投票"));
            return;
        }

        // 找出最高得票者
        UUID winnerId = null;
        int maxVotes = 0;
        for (Entry<UUID, Integer> entry : tally.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                winnerId = entry.getKey();
            }
        }

        Player winner = Bukkit.getPlayer(winnerId);
        if (winner == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "得票者已离线，无人被惩罚");
            recordResult("得票者离线，未执行惩罚");
            return;
        }

        // 广播结果
        String resultMsg = Messages.getString("result-broadcast")
            .replace("%winner%", winner.getName())
            .replace("%votes%", String.valueOf(maxVotes));
        Bukkit.broadcastMessage(resultMsg);

        // 击杀得票者
        winner.setHealth(0);

        recordResult(winner.getName() + " 获得 " + maxVotes + " 票，已被处决");
    }

    public void cancelVote() {
        if (!active) return;
        active = false;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        Bukkit.broadcastMessage(ChatColor.YELLOW + "投票已取消");
    }

    public void shutdown() {
        if (active) {
            cancelVote();
        }
    }

    private void recordResult(String line) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File file = new File(dataFolder, "vote_results.txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println("[" + time + "] " + line);
        } catch (IOException e) {
            plugin.getLogger().warning("无法写入投票记录: " + e.getMessage());
        }
    }
}