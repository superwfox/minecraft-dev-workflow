package com.tahai.trollplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrankManager {
    private final Plugin plugin;
    private final Map<UUID, Map<PrankType, Object>> prankData = new ConcurrentHashMap<>();

    public enum PrankType {
        REVERSE_CONTROLS,
        FAKE_BOSSBAR,
        GRAVITY_REVERSE,
        RANDOM_WEATHER,
        EXPLOSION,
        MOB_INVASION,
        SOUND,
        CREEPER_STALKER,
        VILLAGER_TRIAL,
        ANIMAL_TALK,
        SNOWMAN_CLONE,
        FORCE_CLEAR_INVENTORY,
        CAT_CANNON,
        DIRT_RAIN
    }

    public PrankManager(Plugin plugin) {
        this.plugin = plugin;
    }

    private Map<PrankType, Object> getPlayerData(Player player) {
        return prankData.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
    }

    // 反向操作（仅存标志，由其他地方处理）
    public void startReverseControls(Player player) {
        getPlayerData(player).put(PrankType.REVERSE_CONTROLS, true);
    }

    // 假BossBar
    public void startFakeBossBar(Player player) {
        BossBar bar = Bukkit.createBossBar("§c假进度条", BarColor.RED, BarStyle.SOLID);
        bar.addPlayer(player);

        BukkitTask task = new BukkitRunnable() {
            double progress = 0.0;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                progress += 0.01;
                if (progress > 1.0) progress = 0.0;
                bar.setProgress(progress);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        getPlayerData(player).put(PrankType.FAKE_BOSSBAR, new BossBarData(bar, task));
    }

    // 重力反转
    public void startGravityReverse(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                player.setFlySpeed(-0.1f);
                player.setWalkSpeed(-0.2f);
            }
        }.runTaskTimer(plugin, 0L, 40L); // 每2秒刷新一下，避免被重置

        getPlayerData(player).put(PrankType.GRAVITY_REVERSE, task);
    }

    // 随机天气
    public void startRandomWeather(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                World world = player.getWorld();
                world.setStorm(!world.hasStorm());
                world.setThundering(!world.isThundering());
            }
        }.runTaskTimer(plugin, 0L, 100L);

        getPlayerData(player).put(PrankType.RANDOM_WEATHER, task);
    }

    // 随机爆炸
    public void startExplosion(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                player.getWorld().createExplosion(loc, 3F, false, false);
            }
        }.runTaskTimer(plugin, 0L, 40L);

        getPlayerData(player).put(PrankType.EXPLOSION, task);
    }

    // 生物入侵
    public void startMobInvasion(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                World world = player.getWorld();
                world.spawnEntity(loc, EntityType.ZOMBIE);
                world.spawnEntity(loc, EntityType.SKELETON);
                world.spawnEntity(loc, EntityType.SPIDER);
            }
        }.runTaskTimer(plugin, 0L, 100L);

        getPlayerData(player).put(PrankType.MOB_INVASION, task);
    }

    // 随机音效
    public void startSound(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                player.playSound(loc, Sound.CREEPER_PRIMED, 1.0f, 1.0f);
            }
        }.runTaskTimer(plugin, 0L, 20L);

        getPlayerData(player).put(PrankType.SOUND, task);
    }

    // 苦力怕跟屁虫
    public void startCreeperStalker(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation().add(0, 2, 2);
                Creeper creeper = (Creeper) player.getWorld().spawnEntity(loc, EntityType.CREEPER);
                creeper.setTarget(player);
            }
        }.runTaskTimer(plugin, 0L, 100L);

        getPlayerData(player).put(PrankType.CREEPER_STALKER, task);
    }

    // 村民审判
    public void startVillagerTrial(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation().add(0, 2, 0);
                player.getWorld().spawnEntity(loc, EntityType.VILLAGER);
            }
        }.runTaskTimer(plugin, 0L, 60L);

        getPlayerData(player).put(PrankType.VILLAGER_TRIAL, task);
    }

    // 动物说话
    public void startAnimalTalk(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation().add(2, 0, 2);
                Sheep sheep = (Sheep) player.getWorld().spawnEntity(loc, EntityType.SHEEP);
                sheep.setCustomName("§c说人话");
                sheep.setCustomNameVisible(true);
                // 这里不实际让动物说话，只是创建带名字的动物
            }
        }.runTaskTimer(plugin, 0L, 80L);

        getPlayerData(player).put(PrankType.ANIMAL_TALK, task);
    }

    // 雪傀儡克隆
    public void startSnowmanClone(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation().add(1, 0, 1);
                player.getWorld().spawnEntity(loc, EntityType.SNOW_GOLEM);
            }
        }.runTaskTimer(plugin, 0L, 70L);

        getPlayerData(player).put(PrankType.SNOWMAN_CLONE, task);
    }

    // 强制背包清空（一次性）
    public void startForceClearInventory(Player player) {
        player.getInventory().clear();
        getPlayerData(player).put(PrankType.FORCE_CLEAR_INVENTORY, true);
    }

    // 猫咪发射炮
    public void startCatCannon(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                Cat cat = (Cat) player.getWorld().spawnEntity(loc, EntityType.CAT);
                Vector direction = player.getLocation().getDirection().normalize().multiply(2);
                cat.setVelocity(direction);
            }
        }.runTaskTimer(plugin, 0L, 40L);

        getPlayerData(player).put(PrankType.CAT_CANNON, task);
    }

    // 泥土方块雨
    public void startDirtRain(Player player) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation().add(0, 10, 0);
                player.getWorld().spawnFallingBlock(loc, Material.DIRT.createBlockData());
            }
        }.runTaskTimer(plugin, 0L, 5L);

        getPlayerData(player).put(PrankType.DIRT_RAIN, task);
    }

    // 停止玩家所有恶搞
    public void stopPlayerAllPranks(Player player) {
        Map<PrankType, Object> data = prankData.remove(player.getUniqueId());
        if (data == null) return;

        for (Map.Entry<PrankType, Object> entry : data.entrySet()) {
            PrankType type = entry.getKey();
            Object resource = entry.getValue();
            switch (type) {
                case FAKE_BOSSBAR:
                    if (resource instanceof BossBarData) {
                        BossBarData bd = (BossBarData) resource;
                        bd.task.cancel();
                        bd.bar.removePlayer(player);
                    }
                    break;
                case GRAVITY_REVERSE:
                case RANDOM_WEATHER:
                case EXPLOSION:
                case MOB_INVASION:
                case SOUND:
                case CREEPER_STALKER:
                case VILLAGER_TRIAL:
                case ANIMAL_TALK:
                case SNOWMAN_CLONE:
                case CAT_CANNON:
                case DIRT_RAIN:
                    if (resource instanceof BukkitTask) {
                        ((BukkitTask) resource).cancel();
                    }
                    break;
                case REVERSE_CONTROLS:
                case FORCE_CLEAR_INVENTORY:
                    // 无需额外操作
                    break;
            }
        }
    }

    // 停止所有恶搞
    public void stopAllPranks() {
        for (UUID uuid : prankData.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                stopPlayerAllPranks(player);
            }
        }
        prankData.clear();
    }

    // 内部类，存储BossBar和任务
    private static class BossBarData {
        final BossBar bar;
        final BukkitTask task;

        BossBarData(BossBar bar, BukkitTask task) {
            this.bar = bar;
            this.task = task;
        }
    }
}