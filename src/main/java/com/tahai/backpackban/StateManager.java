package com.tahai.backpackban;

import org.bukkit.plugin.Plugin;

import java.io.*;

public class StateManager {
    private final File stateFile;
    private boolean enabled;

    public StateManager(Plugin plugin) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.stateFile = new File(dataFolder, "state.txt");
        load();
    }

    private void load() {
        if (!stateFile.exists()) {
            enabled = false;
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(stateFile))) {
            String line = reader.readLine();
            enabled = "true".equalsIgnoreCase(line != null ? line.trim() : "");
        } catch (IOException e) {
            enabled = false;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(stateFile))) {
            writer.write(enabled ? "true" : "false");
        } catch (IOException e) {
            // ignore
        }
    }
}