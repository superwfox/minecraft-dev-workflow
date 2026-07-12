package com.tahai.prankplugin;

import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrankTaskManager {
    private final Map<UUID, Map<String, Object>> tasks = new HashMap<>();

    public void addTask(UUID playerId, String taskKey, Object taskHandle) {
        tasks.computeIfAbsent(playerId, k -> new HashMap<>()).put(taskKey, taskHandle);
    }

    public void cancelPlayerTasks(UUID playerId) {
        Map<String, Object> playerTasks = tasks.remove(playerId);
        if (playerTasks != null) {
            for (Object handle : playerTasks.values()) {
                if (handle instanceof BukkitTask) {
                    ((BukkitTask) handle).cancel();
                }
            }
        }
    }

    public void cancelAllTasks() {
        for (UUID playerId : new ArrayList<>(tasks.keySet())) {
            cancelPlayerTasks(playerId);
        }
    }

    public void shutdown() {
        cancelAllTasks();
    }

    public void save() {
        // Tasks are runtime only, no persistent state to save.
    }
}