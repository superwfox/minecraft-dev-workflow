package com.tahai.sect;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class ClanPlaceholderExpansion extends PlaceholderExpansion {

    private final ClanManager clanManager;

    public ClanPlaceholderExpansion(ClanManager clanManager) {
        this.clanManager = clanManager;
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
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("zw")) {
            String role = clanManager.getRole(player.getUniqueId());
            return role == null ? "" : role;
        }

        if (params.equalsIgnoreCase("zm")) {
            String clan = clanManager.getClanName(player.getUniqueId());
            return clan == null ? "" : clan;
        }

        return null;
    }
}