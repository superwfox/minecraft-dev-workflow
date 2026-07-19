package com.tahai.arenapvp;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class PapiExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "arenapvp";
    }

    @Override
    public String getAuthor() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ArenaPVP");
        if (plugin != null && !plugin.getDescription().getAuthors().isEmpty()) {
            return plugin.getDescription().getAuthors().get(0);
        }
        return "Tahai";
    }

    @Override
    public String getVersion() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ArenaPVP");
        return plugin != null ? plugin.getDescription().getVersion() : "1.0";
    }

    @Override
    public String getRequiredPlugin() {
        return "ArenaPVP";
    }

    @Override
    public boolean persist() {
        return false;
    }

    private ArenaManager getArenaManager() {
        RegisteredServiceProvider<ArenaManager> rsp = Bukkit.getServicesManager().getRegistration(ArenaManager.class);
        if (rsp != null) {
            return rsp.getProvider();
        }
        return null;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return null;
        }
        ArenaManager arenaManager = getArenaManager();
        if (arenaManager == null) {
            return "";
        }
        UUID uuid = player.getUniqueId();
        switch (identifier) {
            case "kills":
                return String.valueOf(arenaManager.getStat(uuid, "kills"));
            case "deaths":
                return String.valueOf(arenaManager.getStat(uuid, "deaths"));
            case "wins":
                return String.valueOf(arenaManager.getStat(uuid, "wins"));
            case "kdr": {
                int kills = arenaManager.getStat(uuid, "kills");
                int deaths = arenaManager.getStat(uuid, "deaths");
                if (deaths == 0) {
                    return kills + ".00";
                }
                double kdr = (double) kills / deaths;
                return String.format("%.2f", kdr);
            }
            default:
                return null;
        }
    }

    @Override
    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("ArenaPVP");
    }
}