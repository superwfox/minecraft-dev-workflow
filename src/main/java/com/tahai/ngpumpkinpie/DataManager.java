package com.tahai.ngpumpkinpie;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DataManager {

    private final Plugin plugin;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void save() {
        // data persistence placeholder
    }

    public void shutdown() {
        save();
    }
}