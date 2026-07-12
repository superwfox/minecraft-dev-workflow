package com.tahai.qqwhitelist;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class VerificationManager {

    private static final long DEFAULT_EXPIRE_SECONDS = 300;

    private final Plugin plugin;
    private final File file;

    public VerificationManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "verification.txt");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> readAllLines() {
        try {
            return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void writeAllLines(List<String> lines) {
        try {
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCode(String code, String playerName, long timestamp) {
        List<String> lines = readAllLines();
        lines.removeIf(line -> {
            String[] parts = line.split("=", 3);
            return parts.length == 3 && parts[0].equals(code);
        });
        lines.add(code + "=" + playerName + "=" + timestamp);
        writeAllLines(lines);
    }

    public String verifyCode(String code) {
        List<String> lines = readAllLines();
        long now = System.currentTimeMillis() / 1000;
        for (String line : lines) {
            String[] parts = line.split("=", 3);
            if (parts.length == 3 && parts[0].equals(code)) {
                long timestamp = Long.parseLong(parts[2]);
                if (now - timestamp <= DEFAULT_EXPIRE_SECONDS) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    public boolean deleteCode(String code) {
        List<String> lines = readAllLines();
        boolean removed = lines.removeIf(line -> {
            String[] parts = line.split("=", 3);
            return parts.length == 3 && parts[0].equals(code);
        });
        if (removed) {
            writeAllLines(lines);
        }
        return removed;
    }

    public void cleanExpired() {
        List<String> lines = readAllLines();
        long now = System.currentTimeMillis() / 1000;
        boolean changed = lines.removeIf(line -> {
            String[] parts = line.split("=", 3);
            if (parts.length == 3) {
                long timestamp = Long.parseLong(parts[2]);
                return now - timestamp > DEFAULT_EXPIRE_SECONDS;
            }
            return false;
        });
        if (changed) {
            writeAllLines(lines);
        }
    }

    public boolean hasUnexpiredCode(String playerName) {
        List<String> lines = readAllLines();
        long now = System.currentTimeMillis() / 1000;
        for (String line : lines) {
            String[] parts = line.split("=", 3);
            if (parts.length == 3 && parts[1].equals(playerName)) {
                long timestamp = Long.parseLong(parts[2]);
                if (now - timestamp <= DEFAULT_EXPIRE_SECONDS) {
                    return true;
                }
            }
        }
        return false;
    }

    public void save() {
    }

    public void shutdown() {
    }
}