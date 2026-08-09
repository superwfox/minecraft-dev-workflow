package com.tahai.sect;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;

public class SectPlaceholderExpansion extends PlaceholderExpansion {

    private final Plugin plugin;

    public SectPlaceholderExpansion(Plugin plugin) {
        this.plugin = plugin;
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
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        if (!identifier.equals("clan_name") && !identifier.equals("rank") && !identifier.equals("clan_kills")) {
            return null;
        }
        YamlConfiguration cfg = loadData();
        ConfigurationSection clan = findPlayerClan(cfg, player.getUniqueId());
        if (clan == null) {
            return "";
        }
        if (identifier.equals("clan_name")) {
            return clan.getName();
        }
        if (identifier.equals("rank")) {
            String rankName = clan.getString("members." + player.getUniqueId());
            if (rankName == null) {
                return "";
            }
            for (SectRank rank : SectRank.values()) {
                if (rank.getName().equals(rankName)) {
                    return rank.getDisplayName();
                }
            }
            return "";
        }
        return String.valueOf(clan.getInt("killCount", 0));
    }

    private YamlConfiguration loadData() {
        File file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private ConfigurationSection findPlayerClan(YamlConfiguration cfg, UUID uuid) {
        ConfigurationSection clans = cfg.getConfigurationSection("clans");
        if (clans == null) {
            return null;
        }
        String uuidStr = uuid.toString();
        for (String name : clans.getKeys(false)) {
            ConfigurationSection members = clans.getConfigurationSection(name + ".members");
            if (members != null && members.getKeys(false).contains(uuidStr)) {
                return clans.getConfigurationSection(name);
            }
        }
        return null;
    }
}