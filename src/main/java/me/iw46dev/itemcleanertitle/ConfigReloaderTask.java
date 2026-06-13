package me.iw46dev.itemcleanertitle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigReloaderTask extends BukkitRunnable {

    private final Map<String, Long> lastModifiedMap = new HashMap<>();

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemCleanerTitle");
        if (plugin == null) return;

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File titlesFile = new File(plugin.getDataFolder(), "titles.yml");

        boolean changed = false;
        if (checkAndUpdate(configFile)) changed = true;
        if (checkAndUpdate(titlesFile)) changed = true;

        if (changed) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.reloadConfig();
                AutoCleanTask.reload();
            });
        }
    }

    private boolean checkAndUpdate(File file) {
        long lastModified = file.lastModified();
        String path = file.getAbsolutePath();
        Long previous = lastModifiedMap.get(path);
        if (previous == null || previous != lastModified) {
            lastModifiedMap.put(path, lastModified);
            return true;
        }
        return false;
    }
}