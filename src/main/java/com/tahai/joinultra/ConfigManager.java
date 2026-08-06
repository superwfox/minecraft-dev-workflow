package com.tahai.joinultra;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Plugin plugin;
    private boolean disableVanillaMessage;
    private String defaultGroup;
    private Map<String, Map<String, String>> groups = new HashMap<>();

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        disableVanillaMessage = config.getBoolean("disable-vanilla-message", true);
        defaultGroup = config.getString("default-group", "default");

        groups.clear();
        ConfigurationSection section = config.getConfigurationSection("groups");
        if (section != null) {
            for (String groupName : section.getKeys(false)) {
                ConfigurationSection groupSection = section.getConfigurationSection(groupName);
                if (groupSection == null) continue;
                Map<String, String> data = new HashMap<>();
                for (String key : groupSection.getKeys(false)) {
                    data.put(key, groupSection.getString(key));
                }
                groups.put(groupName, data);
            }
        }
    }

    public boolean isDisableVanillaMessage() {
        return disableVanillaMessage;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public Map<String, Map<String, String>> getGroups() {
        return groups;
    }

    public void save() {
        plugin.saveConfig();
    }

    public void shutdown() {
        groups.clear();
    }
}