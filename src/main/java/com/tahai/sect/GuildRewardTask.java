package com.tahai.sect;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

public class GuildRewardTask extends BukkitRunnable {
    private static final long REWARD_INTERVAL = 12L * 60L * 60L * 1000L;

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            return;
        }
        Economy economy = getEconomy();
        if (economy == null) {
            return;
        }
        ConfigurationSection guilds = plugin.getConfig().getConfigurationSection("guilds");
        if (guilds == null) {
            return;
        }
        long now = System.currentTimeMillis();
        for (String guildName : guilds.getKeys(false)) {
            long lastReward = plugin.getConfig().getLong("guilds." + guildName + ".coinRewardTime", 0L);
            if (now - lastReward < REWARD_INTERVAL) {
                continue;
            }
            String leaderName = plugin.getConfig().getString("guilds." + guildName + ".leader");
            if (leaderName == null || leaderName.isEmpty()) {
                continue;
            }
            int level = plugin.getConfig().getInt("guilds." + guildName + ".level", 1);
            double amount = getRewardAmount(level);
            OfflinePlayer leader = Bukkit.getOfflinePlayer(leaderName);
            EconomyResponse response = economy.depositPlayer(leader, amount);
            if (response.transactionSuccess()) {
                plugin.getConfig().set("guilds." + guildName + ".coinRewardTime", now);
                plugin.saveConfig();
            }
        }
    }

    private Economy getEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }
        return rsp.getProvider();
    }

    private double getRewardAmount(int level) {
        switch (level) {
            case 1:
                return 500_000_000.0;
            case 2:
                return 1_000_000_000.0;
            case 3:
                return 2_000_000_000.0;
            case 4:
                return 3_000_000_000.0;
            case 5:
                return 4_000_000_000.0;
            default:
                return 6_000_000_000.0;
        }
    }
}