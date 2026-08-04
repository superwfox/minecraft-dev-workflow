package com.tahai.pvpduel;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DuelManager duelManager;

    @Override
    public void onEnable() {
        this.duelManager = new DuelManager();

        VSCommand vsCommand = new VSCommand();
        getCommand("vs").setExecutor(vsCommand);
        getCommand("vs").setTabCompleter(vsCommand);

        AcceptCommand acceptCommand = new AcceptCommand(duelManager);
        getCommand("accept").setExecutor(acceptCommand);
        getCommand("accept").setTabCompleter(acceptCommand);

        getServer().getPluginManager().registerEvents(new DuelGUIClickListener(duelManager), this);
        getServer().getPluginManager().registerEvents(new DuelDeathListener(duelManager), this);

        new InviteExpireTask(duelManager).runTaskTimer(this, 20L, 20L);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }
}