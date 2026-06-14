package com.example.coppersword;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CooldownManager {

    private final File dataFile;
    private final YamlConfiguration config;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public CooldownManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CopperSword");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "cooldowns.yml");
        config = YamlConfiguration.loadConfiguration(dataFile);
        loadCooldowns();
    }

    private void loadCooldowns() {
        if (config.contains("cooldowns")) {
            for (String key : config.getConfigurationSection("cooldowns").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                long expire = config.getLong("cooldowns." + key);
                cooldowns.put(uuid, expire);
            }
        }
    }

    public boolean isOnCooldown(UUID playerUuid) {
        Long expire = cooldowns.get(playerUuid);
        if (expire == null) return false;
        if (System.currentTimeMillis() > expire) {
            cooldowns.remove(playerUuid);
            return false;
        }
        return true;
    }

    public boolean isOnCooldown(Player player) {
        return isOnCooldown(player.getUniqueId());
    }

    public void setCooldown(UUID playerUuid, long durationMillis) {
        cooldowns.put(playerUuid, System.currentTimeMillis() + durationMillis);
    }

    public void setCooldown(Player player, long durationMillis) {
        setCooldown(player.getUniqueId(), durationMillis);
    }

    public void save() {
        config.set("cooldowns", null);
        for (Map.Entry<UUID, Long> entry : cooldowns.entrySet()) {
            config.set("cooldowns." + entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("CopperSword");
            plugin.getLogger().log(Level.SEVERE, "Could not save cooldowns.yml", e);
        }
    }
}