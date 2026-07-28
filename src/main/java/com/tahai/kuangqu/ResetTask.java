package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResetTask extends BukkitRunnable {

    private final MineManager manager;
    private final Map<String, Long> lastBroadcast = new HashMap<>();

    public ResetTask(MineManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Kuangqu");
        if (plugin == null) return;
        JavaPlugin jp = (JavaPlugin) plugin;
        int countdownSeconds = jp.getConfig().getInt("countdown-seconds", 60);
        int broadcastInterval = jp.getConfig().getInt("broadcast-interval", 10);
        String reminderTemplate = jp.getConfig().getString("messages.countdown-reminder",
                ChatColor.YELLOW + "矿区将在 %time% 秒后重置！");

        List<Mine> mines = manager.getAllMines();
        long now = System.currentTimeMillis();

        for (Mine mine : mines) {
            String[] parts = mine.getResetTime().split(":");
            if (parts.length != 2) continue;
            int hour, minute;
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                continue;
            }

            Calendar resetCal = Calendar.getInstance();
            resetCal.set(Calendar.HOUR_OF_DAY, hour);
            resetCal.set(Calendar.MINUTE, minute);
            resetCal.set(Calendar.SECOND, 0);
            resetCal.set(Calendar.MILLISECOND, 0);
            long resetMillis = resetCal.getTimeInMillis();

            if (mine.getLastReset() >= resetMillis) {
                lastBroadcast.remove(mine.getName());
                continue;
            }

            if (now >= resetMillis) {
                World world = Bukkit.getWorld(mine.getWorldName());
                if (world == null) continue;

                Location exitLoc = new Location(world, mine.getMinX() - 1, mine.getMinY(), mine.getMinZ());
                for (Player player : world.getPlayers()) {
                    if (mine.contains(player.getLocation())) {
                        player.teleport(exitLoc);
                    }
                }

                manager.resetMine(mine);
                mine.setLastReset(now);
                manager.save();
                lastBroadcast.remove(mine.getName());
            } else {
                long remaining = (resetMillis - now) / 1000;
                if (remaining > 0 && remaining <= countdownSeconds) {
                    long lastBc = lastBroadcast.getOrDefault(mine.getName(), 0L);
                    if (now - lastBc >= broadcastInterval * 1000L) {
                        World world = Bukkit.getWorld(mine.getWorldName());
                        if (world == null) continue;
                        String message = reminderTemplate.replace("%time%", String.valueOf(remaining));
                        for (Player player : world.getPlayers()) {
                            if (mine.contains(player.getLocation())) {
                                player.sendMessage(message);
                            }
                        }
                        lastBroadcast.put(mine.getName(), now);
                    }
                }
            }
        }
    }
}