package com.tahai.sect;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.UUID;

public class SectIncomeTask extends BukkitRunnable {

    private static final double[] INCOME_BY_LEVEL = {
            500_000_000D,
            1_000_000_000D,
            2_000_000_000D,
            3_000_000_000D,
            4_000_000_000D,
            6_000_000_000D
    };

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("未找到 Vault，宗门俸禄发放跳过。");
            return;
        }

        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            plugin.getLogger().warning("未找到 Vault 经济服务，宗门俸禄发放跳过。");
            return;
        }

        Economy economy = provider.getProvider();
        if (economy == null) {
            return;
        }

        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection clans = data.getConfigurationSection("clans");
        if (clans == null) {
            return;
        }

        for (String name : clans.getKeys(false)) {
            ConfigurationSection clan = clans.getConfigurationSection(name);
            if (clan == null) {
                continue;
            }

            String leaderUuid = clan.getString("leader");
            if (leaderUuid == null) {
                continue;
            }

            UUID uuid;
            try {
                uuid = UUID.fromString(leaderUuid);
            } catch (IllegalArgumentException ex) {
                continue;
            }

            int level = Math.max(1, Math.min(6, clan.getInt("level", 1)));
            double amount = INCOME_BY_LEVEL[level - 1];

            OfflinePlayer leader = Bukkit.getOfflinePlayer(uuid);
            economy.depositPlayer(leader, amount);

            plugin.getLogger().info("宗门 " + name + " 获得俸禄: " + economy.format(amount));
        }
    }
}