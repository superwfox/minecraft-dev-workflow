package com.tahai.authwebmanager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModManager {
    private final File modsDir;
    private final File metaFile;
    private final Map<String, ModInfo> mods = new LinkedHashMap<>();

    public ModManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AuthWebManager");
        if (plugin == null) {
            throw new IllegalStateException("AuthWebManager plugin not found");
        }
        File dataFolder = plugin.getDataFolder();
        String path = "mods";
        File configFile = new File(dataFolder, "config.yml");
        if (configFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            path = config.getString("mods.path", "mods");
        }
        Path dir = Paths.get(path);
        if (!dir.isAbsolute()) {
            dir = dataFolder.toPath().getParent().getParent().resolve(path).normalize();
        }
        this.modsDir = dir.toFile();
        this.metaFile = new File(dataFolder, "mods.txt");
        if (!modsDir.exists()) {
            modsDir.mkdirs();
        }
        loadMeta();
        scanMods();
    }

    public synchronized List<ModInfo> listMods() {
        scanMods();
        return new ArrayList<>(mods.values());
    }

    public synchronized File downloadMod(String name) {
        ModInfo info = mods.get(name);
        if (info == null) return null;
        File file = new File(modsDir, info.getCore());
        return file.isFile() ? file : null;
    }

    public synchronized boolean uploadMod(String name, String version, String core, InputStream data) {
        if (name == null || name.isBlank() || core == null || core.isBlank() || data == null) return false;
        if (name.contains("|") || name.contains("\n") || name.contains("\r")) return false;
        if (core.contains("|") || core.contains("\n") || core.contains("\r") || core.contains("/") || core.contains("\\") || core.contains("..")) return false;
        if (version != null && (version.contains("|") || version.contains("\n") || version.contains("\r"))) return false;
        if (!modsDir.exists()) modsDir.mkdirs();
        File target = new File(modsDir, core);
        try {
            Files.copy(data, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            ModInfo old = mods.get(name);
            if (old != null && !old.getCore().equals(core)) {
                File oldFile = new File(modsDir, old.getCore());
                if (oldFile.exists()) oldFile.delete();
            }
            mods.put(name, new ModInfo(name, version == null || version.isBlank() ? "unknown" : version, core));
            saveMeta();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean deleteMod(String name) {
        ModInfo info = mods.get(name);
        if (info == null) return false;
        File file = new File(modsDir, info.getCore());
        if (file.exists() && !file.delete()) return false;
        mods.remove(name);
        saveMeta();
        return true;
    }

    public synchronized void save() {
        saveMeta();
    }

    public synchronized void shutdown() {
        save();
    }

    private void scanMods() {
        File[] files = modsDir.listFiles();
        if (files == null) return;
        Set<String> present = new HashSet<>();
        Map<String, ModInfo> newMods = new HashMap<>();
        for (File f : files) {
            if (!f.isFile()) continue;
            String core = f.getName();
            ModInfo byCore = null;
            for (ModInfo info : mods.values()) {
                if (info.getCore().equals(core)) {
                    byCore = info;
                    break;
                }
            }
            if (byCore != null) {
                present.add(byCore.getName());
                continue;
            }
            String name = core;
            int dot = core.lastIndexOf('.');
            if (dot > 0) {
                name = core.substring(0, dot);
            }
            ModInfo existing = mods.get(name);
            if (existing != null) {
                existing.setCore(core);
                present.add(name);
            } else {
                newMods.put(name, new ModInfo(name, "unknown", core));
                present.add(name);
            }
        }
        mods.putAll(newMods);
        mods.keySet().removeIf(k -> !present.contains(k));
        saveMeta();
    }

    private void loadMeta() {
        if (!metaFile.isFile()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(metaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    mods.put(parts[0], new ModInfo(parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveMeta() {
        if (metaFile.getParentFile() != null) {
            metaFile.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(metaFile))) {
            for (ModInfo info : mods.values()) {
                writer.write(info.getName() + "|" + info.getVersion() + "|" + info.getCore());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ModInfo {
        private final String name;
        private String version;
        private String core;

        private ModInfo(String name, String version, String core) {
            this.name = name;
            this.version = version;
            this.core = core;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getCore() {
            return core;
        }

        private void setVersion(String version) {
            this.version = version;
        }

        private void setCore(String core) {
            this.core = core;
        }
    }
}