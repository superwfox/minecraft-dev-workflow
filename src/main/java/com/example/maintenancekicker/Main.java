package com.example.maintenancekicker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin implements Listener {
    private FileConfiguration messagesConfig;
    private File messagesFile;

    @Override
    public void onEnable() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesConfig = new YamlConfiguration();
            messagesConfig.set("notice", "&cServer is under maintenance. Please come back later.");
            saveMessages();
        } else {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            if (!messagesConfig.contains("notice")) {
                messagesConfig.set("notice", "&cServer is under maintenance. Please come back later.");
                saveMessages();
            }
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveMessages();
    }

    private void saveMessages() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            getLogger().severe("Could not save messages.yml: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setNotice")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /setNotice <message>");
                return true;
            }
            String notice = String.join(" ", args);
            messagesConfig.set("notice", notice);
            saveMessages();
            sender.sendMessage("Notice has been updated.");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) {
            String notice = messagesConfig.getString("notice", "&cServer is under maintenance.");
            String kickMessage = ChatColor.translateAlternateColorCodes('&', notice);
            player.kickPlayer(kickMessage);
        }
    }
}