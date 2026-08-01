package com.tahai.sect;

import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class SectPapiExpansion extends PlaceholderExpansion {

    private final SectDataManager dataManager;

    public SectPapiExpansion(SectDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public String getIdentifier() {
        return "sect";
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
    public String getRequiredPlugin() {
        return "Sect";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        if (params == null) {
            return "";
        }

        Sect sect = findSect(player.getUniqueId());
        if (sect == null) {
            return "";
        }

        if (params.equalsIgnoreCase("clan")) {
            return sect.getName();
        }

        if (params.equalsIgnoreCase("role")) {
            return sect.getRole(player.getUniqueId());
        }

        return "";
    }

    private Sect findSect(UUID uuid) {
        for (Sect sect : dataManager.getSects().values()) {
            if (sect.isMember(uuid)) {
                return sect;
            }
        }
        return null;
    }
}