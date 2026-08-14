package com.tahai.lfcworldban;

import com.tahai.lfcworldban.BanManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class LFCWorldBanExpansion extends PlaceholderExpansion {

    private final BanManager banManager;

    public LFCWorldBanExpansion(BanManager banManager) {
        this.banManager = banManager;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            register();
        }
    }

    @Override
    public String getIdentifier() {
        return "lfcworldban";
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
    public String onPlaceholderRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("banned_item")) {
            List<ItemStack> forcedItems = banManager.getForcedItems(player);
            if (forcedItems == null || forcedItems.isEmpty()) {
                return "";
            }

            return forcedItems.stream()
                    .map(item -> item.getType().getKey().toString())
                    .distinct()
                    .collect(Collectors.joining(", "));
        }

        return null;
    }
}