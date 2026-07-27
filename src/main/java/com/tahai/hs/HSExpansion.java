package com.tahai.hs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class HSExpansion extends PlaceholderExpansion {

    private StatsManager statsManager;

    @Override
    public String getIdentifier() {
        return "hs";
    }

    @Override
    public String getAuthor() {
        return "tahai";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    private StatsManager getStatsManager() {
        if (statsManager == null) {
            Plugin hsPlugin = Bukkit.getPluginManager().getPlugin("HS");
            if (hsPlugin instanceof Main) {
                statsManager = ((Main) hsPlugin).getStatsManager();
            }
        }
        return statsManager;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        StatsManager sm = getStatsManager();
        if (sm == null) {
            return "";
        }

        StatsData data = sm.getStats(player);
        if (data == null) {
            return "0";
        }

        if ("recycles".equalsIgnoreCase(params)) {
            return String.valueOf(data.getRecycleCount());
        } else if ("gold".equalsIgnoreCase(params)) {
            return String.valueOf(data.getTotalCoins());
        }

        return null;
    }
}