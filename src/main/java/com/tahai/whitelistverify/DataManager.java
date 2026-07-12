package com.tahai.whitelistverify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataManager {
    private final File dataFile;

    public DataManager(Plugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "codes.txt");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void add(String playerName, String code) {
        long expireTime = System.currentTimeMillis() + 5 * 60 * 1000L;
        JsonObject obj = new JsonObject();
        obj.addProperty("playerName", playerName);
        obj.addProperty("code", code);
        obj.addProperty("expireTime", expireTime);
        List<String> lines = readAllLines();
        lines.add(obj.toString());
        writeAllLines(lines);
    }

    public boolean remove(String key) {
        if (key == null || key.isEmpty()) return false;
        List<String> lines = readAllLines();
        boolean removed = false;
        Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            String line = it.next().trim();
            if (line.isEmpty()) continue;
            try {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                String playerName = obj.get("playerName").getAsString();
                String code = obj.get("code").getAsString();
                if (playerName.equals(key) || code.equals(key)) {
                    it.remove();
                    removed = true;
                }
            } catch (JsonSyntaxException ignored) {
            }
        }
        if (removed) {
            writeAllLines(lines);
        }
        return removed;
    }

    public JsonObject find(String key) {
        if (key == null || key.isEmpty()) return null;
        for (String line : readAllLines()) {
            line = line.trim();
            if (line.isEmpty()) continue;
            try {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                long expireTime = obj.get("expireTime").getAsLong();
                if (System.currentTimeMillis() > expireTime) continue;
                String playerName = obj.get("playerName").getAsString();
                String code = obj.get("code").getAsString();
                if (playerName.equals(key) || code.equals(key)) {
                    return obj;
                }
            } catch (JsonSyntaxException ignored) {
            }
        }
        return null;
    }

    public void cleanExpired() {
        List<String> lines = readAllLines();
        boolean changed = false;
        Iterator<String> it = lines.iterator();
        while (it.hasNext()) {
            String line = it.next().trim();
            if (line.isEmpty()) continue;
            try {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                long expireTime = obj.get("expireTime").getAsLong();
                if (System.currentTimeMillis() > expireTime) {
                    it.remove();
                    changed = true;
                }
            } catch (JsonSyntaxException ignored) {
            }
        }
        if (changed) {
            writeAllLines(lines);
        }
    }

    public void save() {
    }

    private List<String> readAllLines() {
        List<String> lines = new ArrayList<>();
        if (!dataFile.exists()) return lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private void writeAllLines(List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}