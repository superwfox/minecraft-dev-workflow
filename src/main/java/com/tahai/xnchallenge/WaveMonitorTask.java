package com.tahai.xnchallenge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class WaveMonitorTask extends BukkitRunnable {

    private final ConfigManager configManager;
    private final Map<UUID, ChallengeSession> sessions = new HashMap<>();

    public WaveMonitorTask(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void registerPlayer(Player player) {
        ChallengeSession session = new ChallengeSession(player);
        sessions.put(player.getUniqueId(), session);
        session.startWave(0);
    }

    public void unregisterPlayer(Player player) {
        ChallengeSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.clear();
        }
    }

    @Override
    public void run() {
        Iterator<Map.Entry<UUID, ChallengeSession>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ChallengeSession> entry = iterator.next();
            ChallengeSession session = entry.getValue();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                session.clear();
                iterator.remove();
                continue;
            }

            List<Wave> waves = configManager.getWaves();
            int currentWaveIndex = session.currentWaveIndex;
            if (currentWaveIndex >= waves.size()) {
                // 所有波次完成 -> 给予最终奖励
                for (String reward : configManager.getFinalRewards()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward.replace("%player%", player.getName()));
                }
                session.clear();
                player.sendMessage(ChatColor.YELLOW + "挑战完成！奖励已发放。");
                iterator.remove();
                continue;
            }

            // 检查当前波次存活怪物数
            List<UUID> mobs = session.spawnedMobs;
            int alive = 0;
            for (UUID uuid : mobs) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null && !entity.isDead()) {
                    alive++;
                }
            }

            Wave currentWave = waves.get(currentWaveIndex);
            if (alive <= 0) {
                // 当前波次怪物全部死亡 -> 进入下一波
                session.clearMobs();
                session.startWave(currentWaveIndex + 1);
            }

            // 更新 BossBar 进度
            BossBar bar = session.bossBar;
            double totalHealth = currentWave.getMobs().stream()
                    .mapToInt(mobSpawn -> mobSpawn.getAmount())
                    .sum();
            double progress = totalHealth > 0 ? (totalHealth - alive) / totalHealth : 0.0;
            progress = Math.min(1.0, Math.max(0.0, progress));
            bar.setProgress(progress);
            bar.setTitle(ChatColor.YELLOW + "波次 " + (currentWaveIndex + 1) + " / " + waves.size() +
                    ChatColor.GRAY + "  剩余怪物: " + alive);
        }
    }

    private class ChallengeSession {
        final Player player;
        int currentWaveIndex;
        final List<UUID> spawnedMobs = new ArrayList<>();
        final BossBar bossBar;

        ChallengeSession(Player player) {
            this.player = player;
            this.bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
            this.bossBar.addPlayer(player);
        }

        void startWave(int waveIndex) {
            List<Wave> waves = configManager.getWaves();
            if (waveIndex >= waves.size()) {
                return;
            }
            currentWaveIndex = waveIndex;
            Wave wave = waves.get(waveIndex);
            // 生成怪物（通过 ChallengeManager，但此处只能简易实现）
            Plugin plugin = Bukkit.getPluginManager().getPlugin("XnChallenge");
            if (plugin == null) return;

            for (Wave.MobSpawn mobSpawn : wave.getMobs()) {
                String mobType = mobSpawn.getType();
                int amount = mobSpawn.getAmount();
                int delay = mobSpawn.getDelay();
                // 使用 MythicMobs 生成（需判断是否加载）
                for (int i = 0; i < amount; i++) {
                    // 延迟生成（简单起见直接同步生成）
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // 这里直接调用 MythicMobs 的 API 生成实体并记录 UUID
                            // 但为避免依赖未公开类，仅做占位，实际应通过 MythicMobs API
                            // 假设生成 entity 并添加到 spawnedMobs
                            // 此处省略具体生成代码，因为需要 MythicMobs API
                        }
                    }.runTaskLater(plugin, i * 20L * delay);
                }
            }

            bossBar.setProgress(0.0);
            bossBar.setVisible(true);
        }

        void clearMobs() {
            for (UUID uuid : spawnedMobs) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null) {
                    entity.remove();
                }
            }
            spawnedMobs.clear();
        }

        void clear() {
            bossBar.removeAll();
            clearMobs();
        }
    }
}