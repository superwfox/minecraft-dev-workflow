package com.tahai.randomevent;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    public static String WsUrl = "";
    public static String GroupId = "";
    public static String AccessToken = "";

    private ConfigManager() {}

    public static void reload(FileConfiguration config) {
        WsUrl = config.getString("ws-url", "");
        GroupId = config.getString("group-id", "");
        AccessToken = config.getString("access-token", "");
    }
}