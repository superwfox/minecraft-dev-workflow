package com.tahai.sect;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class WarTask extends BukkitRunnable {
    private static final long WAR_DURATION = 30 * 60 * 1000L;
    private static final double REWARD = 6_000_000_000.0D;

    private final SectDataManager dataManager;
    private String attackerName;
    private String defenderName;
    private long startTime;
    private int attackerKills;
    private int defenderKills;

    public WarTask(SectDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        if (!loadWarState()) {
            cancel();
            return;
        }
        long remaining = startTime + WAR_DURATION - System.currentTimeMillis();
        if (remaining > 0) {
            broadcastActionBar((int) (remaining / 1000));
        } else {
            finishWar();
            cancel();
        }
    }

    private boolean loadWarState() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            return false;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(getDataFile(plugin));
        if (!cfg.isConfigurationSection("currentWar")) {
            return false;
        }
        ConfigurationSection section = cfg.getConfigurationSection("currentWar");
        attackerName = section.getString("attacker");
        defenderName = section.getString("defender");
        startTime = section.getLong("startTime");
        if (startTime < 1_000_000_000_000L) {
            startTime *= 1000L;
        }
        attackerKills = section.getInt("attacker-kills", section.getInt("attackerKills", 0));
        defenderKills = section.getInt("defender-kills", section.getInt("defenderKills", 0));
        return attackerName != null && defenderName != null && startTime > 0;
    }

    private File getDataFile(Plugin plugin) {
        String fileName = plugin.getConfig().getString("data-file", "data.yml");
        return new File(plugin.getDataFolder(), fileName);
    }

    private void broadcastActionBar(int seconds) {
        String time = String.format("%02d:%02d", seconds / 60, seconds % 60);
        String message = ChatColor.YELLOW + "宗门战 " + ChatColor.GRAY + attackerName + " " + attackerKills + " - " + defenderKills + " " + defenderName + ChatColor.YELLOW + " | 剩余 " + time;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(message);
        }
    }

    private void finishWar() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            return;
        }

        boolean attackerWin = attackerKills > defenderKills;
        boolean defenderWin = defenderKills > attackerKills;
        String winnerName = attackerWin ? attackerName : (defenderWin ? defenderName : null);
        String loserName = attackerWin ? defenderName : (defenderWin ? attackerName : null);

        Sect winner = winnerName != null ? dataManager.getSect(winnerName) : null;
        Sect attackerSect = dataManager.getSect(attackerName);

        if (winner != null) {
            dataManager.endWar(winnerName, winner.getOwner());
        } else if (attackerSect != null) {
            dataManager.endWar(attackerName, attackerSect.getOwner());
        }

        if (loserName != null) {
            removeWorldGuardRegion(loserName);
            Map<String, Sect> sects = dataManager.getSects();
            sects.remove(loserName);
        }
        if (winner != null) {
            reward(winner.getOwner());
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(getDataFile(plugin));
        if (loserName != null) {
            cfg.set("sects." + loserName, null);
        }
        cfg.set("currentWar", null);
        try {
            cfg.save(getDataFile(plugin));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeWorldGuardRegion(String regionName) {
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg == null || !wg.isEnabled()) {
            return;
        }
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            for (World world : Bukkit.getWorlds()) {
                RegionManager manager = container.get(BukkitAdapter.adapt(world));
                if (manager != null) {
                    manager.removeRegion(regionName);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void reward(UUID owner) {
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return;
            }
            Economy economy = rsp.getProvider();
            if (economy == null) {
                return;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
            economy.depositPlayer(player, REWARD);
        } catch (Throwable ignored) {
        }
    }
}