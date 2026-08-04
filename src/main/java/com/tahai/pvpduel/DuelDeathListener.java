package com.tahai.pvpduel;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class DuelDeathListener implements Listener {

    private final DuelManager duelManager;
    private final Economy economy;

    public DuelDeathListener(DuelManager duelManager) {
        this.duelManager = duelManager;
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        this.economy = provider != null ? provider.getProvider() : null;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = (Player) event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;
        if (!duelManager.isInDuel(victim.getUniqueId()) || !duelManager.isInDuel(killer.getUniqueId())) return;

        boolean rewardTransferred = false;
        if (economy != null) {
            EconomyResponse withdraw = economy.withdrawPlayer(victim, 5000);
            if (withdraw.transactionSuccess()) {
                EconomyResponse deposit = economy.depositPlayer(killer, 5000);
                if (deposit.transactionSuccess()) {
                    rewardTransferred = true;
                } else {
                    // Rollback: give money back to victim
                    EconomyResponse rollback = economy.depositPlayer(victim, 5000);
                    if (!rollback.transactionSuccess()) {
                        Bukkit.getLogger().warning("Failed to rollback duel reward: could not return 5000 to " + victim.getName());
                    }
                    Bukkit.getLogger().warning("Could not transfer 5000 to " + killer.getName() + " for duel reward; reward rolled back.");
                }
            } else {
                Bukkit.getLogger().warning("Could not withdraw 5000 from " + victim.getName() + " for duel reward.");
            }
        } else {
            Bukkit.getLogger().warning("Vault economy not found; duel reward skipped.");
        }

        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "PvpDuel" + ChatColor.GRAY + "] "
                + ChatColor.YELLOW + killer.getName() + ChatColor.GRAY + " defeated "
                + ChatColor.YELLOW + victim.getName() + ChatColor.GRAY + " in a duel!");
        if (rewardTransferred) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "PvpDuel" + ChatColor.GRAY + "] "
                    + ChatColor.YELLOW + killer.getName() + ChatColor.GRAY + " received "
                    + ChatColor.YELLOW + "5000" + ChatColor.GRAY + " coins reward!");
        }

        duelManager.removeFromDuel(victim.getUniqueId());
        duelManager.removeFromDuel(killer.getUniqueId());
    }
}