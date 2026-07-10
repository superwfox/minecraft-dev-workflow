package com.tahai.randomevent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EventTaskManager {

    private static Plugin plugin;
    private static boolean paused;
    private static final Map<UUID, PlayerTask> tasks = new HashMap<>();
    private static final Map<UUID, BukkitTask> cleanupTasks = new HashMap<>();

    private static class PlayerTask {
        BukkitTask reminder;
        BukkitTask trigger;

        void cancelAll() {
            if (reminder != null) {
                reminder.cancel();
                reminder = null;
            }
            if (trigger != null) {
                trigger.cancel();
                trigger = null;
            }
        }
    }

    public static void start(Plugin pluginInstance) {
        plugin = pluginInstance;
        paused = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            schedulePlayer(player.getUniqueId());
        }
    }

    public static void shutdown() {
        for (PlayerTask pt : tasks.values()) {
            pt.cancelAll();
        }
        tasks.clear();
        for (BukkitTask ct : cleanupTasks.values()) {
            ct.cancel();
        }
        cleanupTasks.clear();
    }

    public static void pause() {
        paused = true;
    }

    public static void resume() {
        paused = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!tasks.containsKey(uuid)) {
                schedulePlayer(uuid);
            }
        }
    }

    public static void forceTrigger(UUID playerUuid) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || !player.isOnline()) return;

        PlayerTask old = tasks.remove(playerUuid);
        if (old != null) old.cancelAll();

        BukkitTask oldCleanup = cleanupTasks.remove(playerUuid);
        if (oldCleanup != null) oldCleanup.cancel();

        executeEvent(player);
    }

    private static void schedulePlayer(UUID uuid) {
        if (paused) return;
        PlayerTask pt = new PlayerTask();
        int delay = ThreadLocalRandom.current().nextInt(6000, 36001); // 5～30 分钟

        // 提前 10 秒提醒
        int reminderDelay = delay - 200;
        if (reminderDelay < 0) reminderDelay = 0;

        pt.reminder = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendActionBar("§e10 秒后你将遭遇随机事件！");
            }
        }, reminderDelay);

        pt.trigger = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                tasks.remove(uuid);
                return;
            }
            executeEvent(p);
        }, delay);

        tasks.put(uuid, pt);
    }

    private static void executeEvent(Player player) {
        UUID uuid = player.getUniqueId();
        EventExecutor executor = new EventExecutor();
        executor.executeRandomEvent(player);
        PlayerDataManager dataManager = new PlayerDataManager(plugin);
        dataManager.addPassCount(uuid);

        // 6 分钟清除任务
        BukkitTask cleanTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            executor.clearEventData(uuid);
            cleanupTasks.remove(uuid);
        }, 7200);
        cleanupTasks.put(uuid, cleanTask);

        // 递归安排下次触发
        schedulePlayer(uuid);
    }
}