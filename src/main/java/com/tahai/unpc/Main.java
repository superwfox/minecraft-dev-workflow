package com.tahai.unpc;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private NpcManager npcManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        npcManager = new NpcManager();

        UnpcCommand unpcCommand = new UnpcCommand(npcManager);
        getCommand("unpc").setExecutor(unpcCommand);
        getCommand("unpc").setTabCompleter(unpcCommand);

        getServer().getPluginManager().registerEvents(new NpcClickListener(npcManager), this);

        new NpcLookTask(npcManager).runTaskTimer(this, 0L, 10L);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (npcManager != null) {
            npcManager.save();
        }
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }
}