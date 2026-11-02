package com.tahai.freezecontrol.freeze;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FreezeManager {

    private final Plugin plugin;
    private final Set<UUID> whitelist = new LinkedHashSet<>();
    private boolean enabled;

    public FreezeManager(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("enabled", false);
        for (String raw : config.getStringList("whitelist")) {
            try {
                whitelist.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        FileConfiguration config = plugin.getConfig();
        config.set("enabled", enabled);
        plugin.saveConfig();
    }

    public boolean isWhitelisted(UUID uuid) {
        return whitelist.contains(uuid);
    }

    public boolean addWhitelist(UUID uuid) {
        if (!whitelist.add(uuid)) {
            return false;
        }
        persistWhitelist();
        return true;
    }

    public boolean removeWhitelist(UUID uuid) {
        if (!whitelist.remove(uuid)) {
            return false;
        }
        persistWhitelist();
        return true;
    }

    public List<String> getWhitelistNames() {
        List<String> names = new ArrayList<>();
        for (UUID uuid : whitelist) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName();
            names.add(name != null ? name : uuid.toString());
        }
        return names;
    }

    private void persistWhitelist() {
        List<String> raw = new ArrayList<>();
        for (UUID uuid : whitelist) {
            raw.add(uuid.toString());
        }
        FileConfiguration config = plugin.getConfig();
        config.set("whitelist", raw);
        plugin.saveConfig();
    }
}
</｜｜DSML｜｜ parameter>
</｜｜DSML｜｜ invoke>
</｜｜DSML｜｜ calls>