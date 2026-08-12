package com.tahai.unpc;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private NpcManager npcManager;

    @Override
    public void onEnable() {
        npcManager = NpcManager.fromPlugin(this);

        UnpcCommand unpcCommand = new UnpcCommand();
        getCommand("unpc").setExecutor(unpcCommand);
        getCommand("unpc").setTabCompleter(unpcCommand);

        getServer().getPluginManager().registerEvents(new NpcInteractListener(), this);
    }

    @Override
    public void onDisable() {
        if (npcManager != null) {
            npcManager.save();
            npcManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }
}