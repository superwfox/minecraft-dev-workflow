package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public final class Messages {
    private static Messages instance;
    private FileConfiguration messages;
    private File messagesFile;

    private Messages() {
        reload();
    }

    public static Messages getInstance() {
        if (instance == null) {
            instance = new Messages();
        }
        return instance;
    }

    public void reload() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Shenpan");
        if (plugin == null) return;
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();
        messagesFile = new File(dataFolder, "messages.yml");
        if (!messagesFile.exists()) {
            try {
                messagesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getString(String key) {
        String msg = messages.getString(key);
        if (msg != null) {
            return ChatColor.translateAlternateColorCodes('&', msg);
        }
        return null;
    }
}