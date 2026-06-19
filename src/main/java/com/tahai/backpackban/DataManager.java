package com.tahai.backpackban;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class DataManager {

    private boolean enabled;
    private final File file;

    public DataManager(File file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!file.exists()) {
            enabled = false;
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            enabled = "true".equalsIgnoreCase(line != null ? line.trim() : "");
        } catch (IOException e) {
            enabled = false;
        }
    }

    public void enable() {
        enabled = true;
        save();
    }

    public void disable() {
        enabled = false;
        save();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void save() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Files.writeString(file.toPath(), enabled ? "true" : "false",
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            // Silent catch – ignore write errors in a game plugin
        }
    }
}