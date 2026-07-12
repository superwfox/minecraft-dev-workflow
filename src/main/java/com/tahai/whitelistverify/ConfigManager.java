package com.tahai.whitelistverify;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager {

    public static String WsUrl;
    public static String GroupId;
    public static String AccessToken;

    public static void reload() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WhiteListVerify");
        if (plugin == null) return;

        FileConfiguration cfg = plugin.getConfig();
        WsUrl = cfg.getString("ws-url");
        GroupId = cfg.getString("group-id");
        AccessToken = cfg.getString("access-token");
    }
}