package com.tahai.playerscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DataManager {
    private final Plugin plugin;
    private final File dataFile;
    private final Map<String, PlayerData> players = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.json");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (dataFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, PlayerData>>(){}.getType();
                Map<String, PlayerData> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    players.putAll(loaded);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load players.json: " + e.getMessage());
            }
        }
    }

    public void updatePlayerLogin(String playerName, String ip) {
        PlayerData data = players.computeIfAbsent(playerName, k -> new PlayerData());
        data.setLastIp(ip);
        data.getHistoryIps().add(ip);
    }

    public List<String> getAccountsByIp(String ip, boolean onlyRecent) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, PlayerData> entry : players.entrySet()) {
            PlayerData data = entry.getValue();
            if (onlyRecent) {
                if (ip.equals(data.getLastIp())) {
                    result.add(entry.getKey());
                }
            } else {
                if (data.getHistoryIps().contains(ip)) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;
    }

    public List<String> getAccountsByPlayer(String name, boolean onlyRecent) {
        PlayerData data = players.get(name);
        if (data == null || data.getLastIp() == null) {
            return Collections.emptyList();
        }
        return getAccountsByIp(data.getLastIp(), onlyRecent);
    }

    public void save() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8)) {
            gson.toJson(players, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save players.json: " + e.getMessage());
        }
    }

    static class PlayerData {
        private String lastIp;
        private Set<String> historyIps = new HashSet<>();

        public PlayerData() {}

        public String getLastIp() {
            return lastIp;
        }

        public void setLastIp(String lastIp) {
            this.lastIp = lastIp;
        }

        public Set<String> getHistoryIps() {
            return historyIps;
        }

        public void setHistoryIps(Set<String> historyIps) {
            this.historyIps = historyIps;
        }
    }
}