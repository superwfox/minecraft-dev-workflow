package com.tahai.bedwarshealth;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Plugin bedWars = Bukkit.getPluginManager().getPlugin("BedWars1058");
        if (bedWars == null || !bedWars.isEnabled()) {
            getLogger().warning("BedWars1058 not found or disabled. Disabling BedwarsHealth.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new HealthListener(this), this);
    }

    @Override
    public void onDisable() {
        // no cleanup needed
    }
}