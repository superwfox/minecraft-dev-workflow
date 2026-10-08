package com.tahai.maceshieldbreak;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class WhitelistManager {

    private final Plugin plugin;
    private final Set<UUID> whitelist = new LinkedHashSet<>();

    public WhitelistManager(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        List<String> raw = config.getStringList("whitelist");
        for (String entry : raw) {
            if (entry == null) {
                continue;
            }
            try {
                whitelist.add(UUID.fromString(entry.trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public boolean isWhitelisted(UUID uuid) {
        return whitelist.contains(uuid);
    }

    public boolean add(UUID uuid) {
        return whitelist.add(uuid);
    }

    public boolean remove(UUID uuid) {
        return whitelist.remove(uuid);
    }

    public void save() {
        List<String> raw = new ArrayList<>();
        for (UUID uuid : whitelist) {
            raw.add(uuid.toString());
        }
        plugin.getConfig().set("whitelist", raw);
        plugin.saveConfig();
    }
}