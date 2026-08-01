package com.tahai.sect;

import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

public class IncomeTask extends BukkitRunnable {

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null || !plugin.isEnabled()) return;

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("Vault not found, skipping sect income distribution");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getLogger().warning("No economy provider found, skipping sect income distribution");
            return;
        }

        Economy economy = rsp.getProvider();
        if (economy == null) return;

        int base = plugin.getConfig().getInt("income.base", 0);
        int perLevel = plugin.getConfig().getInt("income.per-level", 100);

        SectDataManager dataManager = new SectDataManager();
        for (Map.Entry<String, Sect> entry : dataManager.getSects().entrySet()) {
            Sect sect = entry.getValue();
            double amount = base + sect.getLevel() * perLevel;
            if (amount <= 0) continue;

            OfflinePlayer owner = Bukkit.getOfflinePlayer(sect.getOwner());
            EconomyResponse response = economy.depositPlayer(owner, amount);
            if (response != null && !response.transactionSuccess()) {
                Bukkit.getLogger().warning("Failed to pay income to sect " + sect.getName() + ": " + response.errorMessage);
            }
        }
    }
}