package com.tahai.kuangqu;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class KuangquPlaceholderExpansion {

    private static final String DEFAULT_ZONE = "野外";

    /**
     * 注册 PlaceholderAPI 占位符扩展。
     * 在主插件 onEnable 时调用此方法即可。
     */
    public static void register() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderExpansion() {
                @Override
                public @NotNull String getIdentifier() {
                    return "kuangqu";
                }

                @Override
                public @NotNull String getAuthor() {
                    return "Tahai";
                }

                @Override
                public @NotNull String getVersion() {
                    return "1.0";
                }

                @Override
                public boolean persist() {
                    return true;
                }

                @Override
                public boolean canRegister() {
                    return true;
                }

                @Override
                public String onPlaceholderRequest(Player player, @NotNull String params) {
                    return KuangquPlaceholderExpansion.onPlaceholderRequest(player, params);
                }
            }.register();
        }
    }

    public static String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        if (params.equalsIgnoreCase("current_zone")) {
            String zoneName = getCurrentZone(player);
            return zoneName != null ? zoneName : DEFAULT_ZONE;
        }
        return null;
    }

    public static String getCurrentZone(Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Kuangqu");
        if (plugin == null) {
            return DEFAULT_ZONE;
        }
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection zones = config.getConfigurationSection("zones");
        if (zones == null) {
            return DEFAULT_ZONE;
        }
        Location loc = player.getLocation();
        for (String key : zones.getKeys(false)) {
            ConfigurationSection zone = zones.getConfigurationSection(key);
            if (zone == null) {
                continue;
            }
            String worldName = zone.getString("world");
            if (worldName == null || !player.getWorld().getName().equals(worldName)) {
                continue;
            }
            int x1 = zone.getInt("x1");
            int y1 = zone.getInt("y1");
            int z1 = zone.getInt("z1");
            int x2 = zone.getInt("x2");
            int y2 = zone.getInt("y2");
            int z2 = zone.getInt("z2");

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            if (loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ) {
                return key;
            }
        }
        return DEFAULT_ZONE;
    }
}