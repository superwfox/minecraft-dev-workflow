package com.tahai.tahaiauth;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final Plugin plugin;
    private final File dataFile;
    private final YamlConfiguration config;
    private final Map<UUID, PlayerData> players;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建 players.yml: " + e.getMessage());
            }
        }
        this.config = YamlConfiguration.loadConfiguration(dataFile);
        this.players = new HashMap<>();
        load();
    }

    private void load() {
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String salt = config.getString(uuidStr + ".salt");
            String hash = config.getString(uuidStr + ".hash");
            boolean authenticated = config.getBoolean(uuidStr + ".authenticated");
            long lastActive = config.getLong(uuidStr + ".lastActive");
            players.put(uuid, new PlayerData(salt, hash, authenticated, lastActive));
        }
    }

    public void save() {
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();
            config.set(uuid.toString() + ".salt", data.salt);
            config.set(uuid.toString() + ".hash", data.hash);
            config.set(uuid.toString() + ".authenticated", data.authenticated);
            config.set(uuid.toString() + ".lastActive", data.lastActive);
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存 players.yml 失败: " + e.getMessage());
        }
    }

    public void shutdown() {
        save();
    }

    public boolean isRegistered(UUID uuid) {
        return players.containsKey(uuid);
    }

    public boolean registerPlayer(UUID uuid, String password) {
        if (players.containsKey(uuid)) return false;
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        players.put(uuid, new PlayerData(salt, hash, false, System.currentTimeMillis()));
        save();
        return true;
    }

    public boolean verifyPlayer(UUID uuid, String password) {
        PlayerData data = players.get(uuid);
        if (data == null) return false;
        return hashPassword(password, data.salt).equals(data.hash);
    }

    public void resetPassword(UUID uuid, String newPassword) {
        PlayerData data = players.get(uuid);
        if (data == null) return;
        data.salt = generateSalt();
        data.hash = hashPassword(newPassword, data.salt);
        data.authenticated = false;
        save();
    }

    public void updateLastActive(UUID uuid) {
        PlayerData data = players.get(uuid);
        if (data != null) {
            data.lastActive = System.currentTimeMillis();
        }
    }

    public void setAuthenticated(UUID uuid, boolean authenticated) {
        PlayerData data = players.get(uuid);
        if (data != null) {
            data.authenticated = authenticated;
        }
    }

    public boolean isAuthenticated(UUID uuid) {
        PlayerData data = players.get(uuid);
        return data != null && data.authenticated;
    }

    private String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static class PlayerData {
        String salt;
        String hash;
        boolean authenticated;
        long lastActive;

        PlayerData(String salt, String hash, boolean authenticated, long lastActive) {
            this.salt = salt;
            this.hash = hash;
            this.authenticated = authenticated;
            this.lastActive = lastActive;
        }
    }
}