package com.tahai.slimeboss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class SlimeBossMain extends JavaPlugin {

    private SlimeBoss slimeBoss;

    @Override
    public void onEnable() {
        Location spawnLoc = Bukkit.getWorlds().get(0).getSpawnLocation();
        slimeBoss = new SlimeBoss(this, spawnLoc);
        getServer().getPluginManager().registerEvents(slimeBoss, this);
        slimeBoss.runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        if (slimeBoss != null) {
            slimeBoss.destroy();
        }
        getServer().getScheduler().cancelTasks(this);
    }
}