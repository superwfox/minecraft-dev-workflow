package com.tahai.xnchallenge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;

import java.util.*;

public class ChallengeManager implements Listener {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, PlayerChallenge> challenges = new HashMap<>();
    private final NamespacedKey cooldownKey;
    private Economy economy;

    public ChallengeManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.cooldownKey = new NamespacedKey(plugin, "challenge_cooldown");
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) economy = rsp.getProvider();
    }

    public void startChallenge(Player player) {
        if (challenges.containsKey(player.getUniqueId())) {
            player.sendMessage(org.bukkit.ChatColor.GRAY + "已在挑战中！");
            return;
        }
        long cooldownEnd = player.getPersistentDataContainer().getOrDefault(cooldownKey, org.bukkit.persistence.PersistentDataType.LONG, 0L);
        if (System.currentTimeMillis() < cooldownEnd) {
            long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
            player.sendMessage(org.bukkit.ChatColor.GRAY + "冷却剩余 " + remaining + " 秒");
            return;
        }
        List<String> allowedWorlds = configManager.getAllowedWorlds();
        if (!allowedWorlds.contains(player.getWorld().getName())) {
            player.sendMessage(org.bukkit.ChatColor.GRAY + "此世界不允许挑战");
            return;
        }
        List<ConfigManager.Wave> waves = configManager.getWaves();
        if (waves.isEmpty()) {
            player.sendMessage(org.bukkit.ChatColor.GRAY + "未配置波次");
            return;
        }
        PlayerChallenge challenge = new PlayerChallenge(player.getUniqueId(), waves);
        challenges.put(player.getUniqueId(), challenge);
        player.sendMessage(org.bukkit.ChatColor.YELLOW + "挑战开始！");
        spawnWave(player, challenge);
    }

    private void spawnWave(Player player, PlayerChallenge challenge) {
        if (challenge.currentWaveIndex >= challenge.waves.size()) {
            finishChallenge(player, challenge);
            return;
        }
        ConfigManager.Wave wave = challenge.waves.get(challenge.currentWaveIndex);
        long waveDelay = wave.getDelay() * 20L;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!challenges.containsKey(player.getUniqueId())) return;
                // 生成怪物
                Set<UUID> mobs = new HashSet<>();
                for (ConfigManager.MobConfig mob : wave.getMobs()) {
                    String type = mob.getType();
                    int amount = mob.getAmount();
                    for (int i = 0; i < amount; i++) {
                        ActiveMob am = MythicBukkit.inst().getMobManager().spawnMob(type, player.getLocation());
                        if (am != null) mobs.add(am.getUniqueId());
                    }
                }
                challenge.aliveMobs = mobs;
                challenge.totalMobs = mobs.size();
                challenge.killed = 0;
                if (challenge.bossBar == null) {
                    challenge.bossBar = Bukkit.createBossBar(
                            "波次 " + (challenge.currentWaveIndex + 1) + "/" + challenge.waves.size(),
                            BarColor.YELLOW,
                            BarStyle.SOLID
                    );
                    challenge.bossBar.addPlayer(player);
                } else {
                    challenge.bossBar.setTitle("波次 " + (challenge.currentWaveIndex + 1) + "/" + challenge.waves.size());
                }
                challenge.bossBar.setProgress(1.0);
                player.sendMessage(org.bukkit.ChatColor.YELLOW + "波次 " + (challenge.currentWaveIndex + 1) + " 开始！");
            }
        }.runTaskLater(plugin, waveDelay);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        UUID uid = entity.getUniqueId();
        for (PlayerChallenge challenge : challenges.values()) {
            if (challenge.aliveMobs != null && challenge.aliveMobs.remove(uid)) {
                challenge.killed++;
                Player player = Bukkit.getPlayer(challenge.playerId);
                if (player == null || !player.isOnline()) return;
                double progress = 1.0 - (double) challenge.aliveMobs.size() / challenge.totalMobs;
                if (challenge.bossBar != null) {
                    challenge.bossBar.setProgress(progress);
                }
                if (challenge.aliveMobs.isEmpty()) {
                    // 波次完成
                    challenge.currentWaveIndex++;
                    player.sendMessage(org.bukkit.ChatColor.YELLOW + "波次完成！");
                    spawnWave(player, challenge);
                }
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        failChallenge(player, "死亡");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        failChallenge(player, "退出游戏");
    }

    private void failChallenge(Player player, String reason) {
        PlayerChallenge challenge = challenges.remove(player.getUniqueId());
        if (challenge == null) return;
        if (challenge.bossBar != null) {
            challenge.bossBar.removePlayer(player);
            challenge.bossBar.removeAll();
        }
        // 清除怪物
        if (challenge.aliveMobs != null) {
            for (UUID uid : challenge.aliveMobs) {
                ActiveMob am = MythicBukkit.inst().getMobManager().getActiveMob(uid).orElse(null);
                if (am != null) am.remove();
            }
        }
        player.sendMessage(org.bukkit.ChatColor.AQUA + "挑战失败： " + reason);
    }

    private void finishChallenge(Player player, PlayerChallenge challenge) {
        challenges.remove(player.getUniqueId());
        if (challenge.bossBar != null) {
            challenge.bossBar.removePlayer(player);
            challenge.bossBar.removeAll();
        }
        // 发放奖励
        List<String> rewards = configManager.getFinalRewards();
        for (String reward : rewards) {
            String[] parts = reward.split(":", 3);
            if (parts.length < 2) continue;
            String type = parts[0].toLowerCase();
            switch (type) {
                case "vault":
                    if (economy != null && parts.length >= 2) {
                        double amount;
                        try { amount = Double.parseDouble(parts[1]); } catch (NumberFormatException e) { break; }
                        economy.depositPlayer(player, amount);
                    }
                    break;
                case "item":
                    if (parts.length >= 3) {
                        org.bukkit.Material mat = org.bukkit.Material.getMaterial(parts[1].toUpperCase());
                        if (mat != null) {
                            int amount;
                            try { amount = Integer.parseInt(parts[2]); } catch (NumberFormatException e) { break; }
                            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(mat, amount));
                        }
                    }
                    break;
                case "command":
                    String cmd = parts.length >= 2 ? parts[1] : "";
                    if (!cmd.isEmpty()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                    }
                    break;
                case "exp":
                    if (parts.length >= 2) {
                        int amount;
                        try { amount = Integer.parseInt(parts[1]); } catch (NumberFormatException e) { break; }
                        player.giveExp(amount);
                    }
                    break;
            }
        }
        // 设置冷却
        int cooldown = configManager.getCooldownSeconds();
        long endTime = System.currentTimeMillis() + cooldown * 1000L;
        player.getPersistentDataContainer().set(cooldownKey, org.bukkit.persistence.PersistentDataType.LONG, endTime);
        player.sendMessage(org.bukkit.ChatColor.YELLOW + "挑战完成！请在 " + cooldown + " 秒后再试。");
    }

    public void shutdown() {
        for (PlayerChallenge challenge : challenges.values()) {
            if (challenge.bossBar != null) {
                challenge.bossBar.removeAll();
            }
            if (challenge.aliveMobs != null) {
                for (UUID uid : challenge.aliveMobs) {
                    ActiveMob am = MythicBukkit.inst().getMobManager().getActiveMob(uid).orElse(null);
                    if (am != null) am.remove();
                }
            }
        }
        challenges.clear();
    }

    private static class PlayerChallenge {
        final UUID playerId;
        final List<ConfigManager.Wave> waves;
        int currentWaveIndex;
        Set<UUID> aliveMobs;
        int totalMobs;
        int killed;
        BossBar bossBar;

        PlayerChallenge(UUID playerId, List<ConfigManager.Wave> waves) {
            this.playerId = playerId;
            this.waves = waves;
            this.currentWaveIndex = 0;
        }
    }
}