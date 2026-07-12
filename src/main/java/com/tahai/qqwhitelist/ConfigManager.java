package com.tahai.qqwhitelist;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private static JavaPlugin plugin;
    private static FileConfiguration config;

    public static String WsUrl;
    public static String GroupId;
    public static String AccessToken;

    public static void init(JavaPlugin p) {
        plugin = p;
        p.saveDefaultConfig();
        reload();
    }

    public static void reload() {
        if (plugin == null) return;
        plugin.reloadConfig();
        config = plugin.getConfig();
        WsUrl = config.getString("ws-url");
        GroupId = config.getString("group-id");
        AccessToken = config.getString("access-token");
    }

    public static void save() {
        if (plugin != null) {
            plugin.saveConfig();
        }
    }
}