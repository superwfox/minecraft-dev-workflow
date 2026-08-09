package com.tahai.sect;

import java.io.File;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class SectCombatListener implements Listener {

    private final DataManager dataManager;

    public SectCombatListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }

        String killerSect = findSect(killer.getUniqueId());
        String victimSect = findSect(victim.getUniqueId());
        if (killerSect == null || victimSect == null || killerSect.equals(victimSect)) {
            return;
        }

        dataManager.addKillCount(killerSect, 1);
    }

    private String findSect(UUID uuid) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            return null;
        }

        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            return null;
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection clans = cfg.getConfigurationSection("clans");
        if (clans == null) {
            return null;
        }

        for (String name : clans.getKeys(false)) {
            if (cfg.contains("clans." + name + ".members." + uuid.toString())) {
                return name;
            }
        }

        return null;
    }
}