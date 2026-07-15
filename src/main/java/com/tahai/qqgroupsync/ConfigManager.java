package com.tahai.qqgroupsync;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ConfigManager {

    private static FileConfiguration config;
    public static String WsUrl;
    public static String GroupId;
    public static String AccessToken;
    public static List<String> AdminQqs;

    private ConfigManager() {}

    public static void load(FileConfiguration cfg) {
        config = cfg;
        WsUrl = config.getString("bot.ws-url");
        GroupId = config.getString("bot.group-id");
        AccessToken = config.getString("bot.access-token");
        AdminQqs = config.getStringList("bot.admin-qqs");
    }

    public static void reload() {
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("QQGroupSync");
        if (plugin != null) {
            plugin.reloadConfig();
            load(plugin.getConfig());
        }
    }

    public static void save() {
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("QQGroupSync");
        if (plugin != null) {
            plugin.saveConfig();
        }
    }

    public static String getString(String path, String def) {
        return config == null ? def : config.getString(path, def);
    }

    public static List<String> getStringList(String path) {
        return config == null ? List.of() : config.getStringList(path);
    }
}