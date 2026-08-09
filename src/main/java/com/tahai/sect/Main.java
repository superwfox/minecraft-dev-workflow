package com.tahai.sect;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static GuildManager manager;
    private GuildManager guildManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        guildManager = new GuildManager(this);
        manager = guildManager;

        SectCommand sectCommand = new SectCommand();
        getCommand("sect").setExecutor(sectCommand);
        getCommand("sect").setTabCompleter(sectCommand);

        getServer().getPluginManager().registerEvents(new RegionSelectorListener(), this);
        getServer().getPluginManager().registerEvents(new WarKillListener(), this);
        getServer().getPluginManager().registerEvents(new JoinSectGuiListener(), this);
        getServer().getPluginManager().registerEvents(new ManageSectGuiListener(), this);

        new GuildRewardTask().runTaskTimer(this, 6000L, 1200L);

        getLogger().info("Sect 已启用");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (guildManager != null) {
            guildManager.shutdown();
            guildManager.save();
        }
        manager = null;
        getLogger().info("Sect 已禁用");
    }

    public static GuildManager getManager() {
        return manager;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }
}