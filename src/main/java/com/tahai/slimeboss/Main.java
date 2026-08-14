package com.tahai.slimeboss;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private SlimeBoss slimeBoss;

    @Override
    public void onEnable() {
        Location spawn = getServer().getWorlds().get(0).getSpawnLocation();
        slimeBoss = new SlimeBoss(this, spawn);
        getServer().getPluginManager().registerEvents(slimeBoss, this);
        slimeBoss.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (slimeBoss != null) {
            slimeBoss.destroy();
        }
        getServer().getScheduler().cancelTasks(this);
    }
}