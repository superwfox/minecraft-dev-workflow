package com.tahai.authweb.manager;

import com.tahai.authweb.model.ModInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModManager {

    private final Map<String, ModInfo> modsById = new LinkedHashMap<>();

    public ModManager(Plugin plugin) {
        loadModsFromDisk(plugin);
    }

    public ModInfo getModById(String id) {
        if (id == null) {
            return null;
        }
        return modsById.get(id.toLowerCase());
    }

    public String getModFilePath(String id) {
        ModInfo mod = getModById(id);
        return mod == null ? null : mod.getFilePath();
    }

    public Collection<ModInfo> getAllMods() {
        return Collections.unmodifiableCollection(new ArrayList<>(modsById.values()));
    }

    public ModInfo uploadMod(File sourceFile, String id, String name, String modVersion,
                             String minecraftVersion, String fileName, Plugin plugin) {
        if (id == null || id.trim().isEmpty() || modsById.containsKey(id.toLowerCase())) {
            return null;
        }
        if (fileName == null || fileName.isEmpty()) {
            fileName = sourceFile.getName();
        }
        File modsDir = new File(plugin.getDataFolder(), "mods");
        if (!modsDir.exists()) {
            modsDir.mkdirs();
        }
        File targetFile = new File(modsDir, id.toLowerCase() + "-" + sourceFile.getName());
        int counter = 1;
        while (targetFile.exists()) {
            targetFile = new File(modsDir, id.toLowerCase() + "-" + counter + "-" + sourceFile.getName());
            counter++;
        }
        try {
            copyFile(sourceFile, targetFile);
        } catch (IOException e) {
            System.err.println("AuthWeb: Failed to copy mod file: " + e.getMessage());
            return null;
        }
        ModInfo info = new ModInfo(id, name, modVersion, minecraftVersion, fileName, targetFile.getAbsolutePath());
        modsById.put(id.toLowerCase(), info);
        saveModsConfig();
        return info;
    }

    public boolean deleteMod(String id) {
        ModInfo mod = getModById(id);
        if (mod == null) {
            return false;
        }
        modsById.remove(id.toLowerCase());
        if (mod.getFilePath() != null) {
            File file = new File(mod.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
        saveModsConfig();
        return true;
    }

    public void save() {
        saveModsConfig();
    }

    public void shutdown() {
        saveModsConfig();
    }

    private void loadModsFromDisk(Plugin plugin) {
        File modsFile = new File(plugin.getDataFolder(), "mods.yml");
        if (!modsFile.exists()) {
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(modsFile);
        ConfigurationSection section = cfg.getConfigurationSection("mods");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".name");
            String modVersion = section.getString(id + ".modVersion");
            String minecraftVersion = section.getString(id + ".minecraftVersion");
            String fileName = section.getString(id + ".fileName");
            String filePath = section.getString(id + ".filePath");
            if (name == null || filePath == null) {
                continue;
            }
            ModInfo info = new ModInfo();
            info.setId(id);
            info.setName(name);
            info.setModVersion(modVersion);
            info.setMinecraftVersion(minecraftVersion);
            info.setFileName(fileName);
            info.setFilePath(filePath);
            modsById.put(id.toLowerCase(), info);
        }
    }

    private void saveModsConfig() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AuthWeb");
        if (plugin == null) {
            return;
        }
        File modsFile = new File(plugin.getDataFolder(), "mods.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        for (ModInfo mod : modsById.values()) {
            String id = mod.getId();
            cfg.set("mods." + id + ".name", mod.getName());
            cfg.set("mods." + id + ".modVersion", mod.getModVersion());
            cfg.set("mods." + id + ".minecraftVersion", mod.getMinecraftVersion());
            cfg.set("mods." + id + ".fileName", mod.getFileName());
            cfg.set("mods." + id + ".filePath", mod.getFilePath());
        }
        try {
            cfg.save(modsFile);
        } catch (IOException e) {
            System.err.println("AuthWeb: Failed to save mods.yml: " + e.getMessage());
        }
    }

    private void copyFile(File source, File target) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
    }
}