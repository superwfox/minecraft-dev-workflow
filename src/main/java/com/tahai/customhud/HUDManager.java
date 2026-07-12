package com.tahai.customhud;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HUDManager {

    private final Plugin plugin;
    private final FileConfiguration config;
    private String rawTemplate;
    private String horizontal;
    private String vertical;
    private String fontNamespace;

    private final Map<UUID, Boolean> visibility = new HashMap<>();

    public HUDManager(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig();
    }

    public void loadConfig() {
        rawTemplate = config.getString("hud-template", "");
        horizontal = config.getString("hud-horizontal", "left");
        vertical = config.getString("hud-vertical", "top");
        fontNamespace = config.getString("custom-font", "minecraft:default");
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfig();
    }

    public void applyToPlayer(Player player) {
        if (!visibility.getOrDefault(player.getUniqueId(), true)) return;

        String formatted = rawTemplate
                .replace("{player}", player.getName())
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));

        Component component = MiniMessage.miniMessage().deserialize(formatted);
        if (fontNamespace != null && !fontNamespace.equals("minecraft:default")) {
            component = component.font(Key.key(fontNamespace));
        }

        player.sendActionBar(component);
    }

    public void removeFromPlayer(Player player) {
        player.sendActionBar(Component.empty());
    }

    public void togglePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boolean current = visibility.getOrDefault(uuid, true);
        visibility.put(uuid, !current);
        if (!current) {
            applyToPlayer(player);
        } else {
            removeFromPlayer(player);
        }
    }

    public void setVisible(Player player, boolean visible) {
        UUID uuid = player.getUniqueId();
        visibility.put(uuid, visible);
        if (visible) {
            applyToPlayer(player);
        } else {
            removeFromPlayer(player);
        }
    }

    public void updateAllPlayers() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            applyToPlayer(online);
        }
    }

    public void shutdown() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            removeFromPlayer(online);
        }
        visibility.clear();
    }

    public boolean isPlayerHidden(Player player) {
        return !visibility.getOrDefault(player.getUniqueId(), true);
    }
}