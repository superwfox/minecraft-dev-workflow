package com.tahai.authwebmanager;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PlayerDataManager {

    private final AuthMeIntegration authMeIntegration;
    private final Logger logger = Bukkit.getLogger();

    public PlayerDataManager() {
        this.authMeIntegration = new AuthMeIntegration();
    }

    public List<OfflinePlayer> getPlayers() {
        List<OfflinePlayer> players = new ArrayList<>();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            players.add(player);
        }
        return players;
    }

    public boolean deletePlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }
        return deletePlayer(Bukkit.getOfflinePlayer(playerName));
    }

    public boolean deletePlayer(OfflinePlayer player) {
        if (player == null) {
            return false;
        }
        deletePlayerData(player);
        deleteAuthMeAccount(player);
        return true;
    }

    private void deletePlayerData(OfflinePlayer player) {
        if (player.getUniqueId() == null) {
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            File playerDataFolder = new File(world.getWorldFolder(), "playerdata");
            File dataFile = new File(playerDataFolder, player.getUniqueId() + ".dat");
            if (dataFile.exists()) {
                dataFile.delete();
            }
        }
    }

    private void deleteAuthMeAccount(OfflinePlayer player) {
        if (player.isOnline()) {
            authMeIntegration.deletePlayer(player.getPlayer());
            return;
        }
        String name = player.getName();
        if (name == null) {
            return;
        }
        try {
            AuthMeApi authMe = AuthMeApi.getInstance();
            if (authMe.isRegistered(name)) {
                authMe.forceUnregister(name);
            }
        } catch (Exception e) {
            logger.warning("Failed to delete AuthMe account for " + name + ": " + e.getMessage());
        }
    }
}