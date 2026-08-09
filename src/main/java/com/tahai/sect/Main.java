package com.tahai.sect;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ClanManager clanManager;
    private ClanPlaceholderExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        clanManager = new ClanManager(this);

        ClanGUI clanGUI = new ClanGUI(clanManager);
        ClanGUIListener guiListener = new ClanGUIListener(clanManager, clanGUI);

        SectCommand sectCommand = new SectCommand(clanManager, clanGUI);
        getCommand("sect").setExecutor(sectCommand);
        getCommand("sect").setTabCompleter(sectCommand);

        getServer().getPluginManager().registerEvents(new ClanEventListener(clanManager), this);
        getServer().getPluginManager().registerEvents(guiListener, this);

        File clanFile = new File(getDataFolder(), "clans.yml");
        if (clanFile.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(clanFile);
            ConfigurationSection clans = cfg.getConfigurationSection("clans");
            if (clans != null) {
                for (String clanId : clans.getKeys(false)) {
                    new RevenueTask(clanId).runTaskTimer(this, 0L, 864000L);
                }
            }
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new ClanPlaceholderExpansion(clanManager);
            placeholderExpansion.register();
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (clanManager != null) {
            clanManager.save();
            clanManager.shutdown();
        }
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public ClanPlaceholderExpansion getPlaceholderExpansion() {
        return placeholderExpansion;
    }
}