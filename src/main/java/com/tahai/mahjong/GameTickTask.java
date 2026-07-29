package com.tahai.mahjong;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameTickTask extends BukkitRunnable {
    private final GameManager gameManager;
    private final Map<String, Long> lastActionTime = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = 30000;

    public GameTickTask(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (String tableId : gameManager.getTableList()) {
            String status = gameManager.getTableStatus(tableId);
            if (status == null) {
                lastActionTime.remove(tableId);
                continue;
            }
            String currentPlayer = extractField(status, "currentPlayer");
            if (currentPlayer == null) {
                lastActionTime.remove(tableId);
                continue;
            }
            long last = lastActionTime.getOrDefault(tableId, now);
            if (now - last > TIMEOUT_MS) {
                Player player = Bukkit.getPlayerExact(currentPlayer);
                if (player != null) {
                    gameManager.playerAction(tableId, player, "discard", null);
                }
                lastActionTime.put(tableId, now);
            } else {
                lastActionTime.putIfAbsent(tableId, now);
            }
        }
    }

    private String extractField(String status, String fieldName) {
        for (String part : status.split(",")) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2 && kv[0].trim().equals(fieldName)) {
                return kv[1].trim();
            }
        }
        return null;
    }
}