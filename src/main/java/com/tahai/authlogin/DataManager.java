package com.tahai.authlogin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.mindrot.jbcrypt.BCrypt;

public class DataManager {
    private final File dataFolder;

    public DataManager(File dataFolder) {
        this.dataFolder = dataFolder;
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public synchronized boolean hasPassword(UUID playerId) {
        return playerFile(playerId).exists();
    }

    public synchronized boolean verifyPassword(UUID playerId, String raw) {
        if (!hasPassword(playerId)) {
            return false;
        }
        String hash = YamlConfiguration.loadConfiguration(playerFile(playerId)).getString("password");
        return hash != null && BCrypt.checkpw(raw, hash);
    }

    public synchronized boolean registerPassword(UUID playerId, String raw) {
        if (hasPassword(playerId)) {
            return false;
        }
        File file = playerFile(playerId);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("password", BCrypt.hashpw(raw, BCrypt.gensalt()));
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void save() {
    }

    public synchronized void shutdown() {
    }

    private File playerFile(UUID playerId) {
        return new File(dataFolder, playerId + ".yml");
    }
}