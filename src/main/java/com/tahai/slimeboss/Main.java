package com.tahai.slimeboss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private SlimeBoss slimeBoss;

    @Override
    public void onEnable() {
        World world = Bukkit.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        this.slimeBoss = new SlimeBoss(spawn, this);
        getServer().getPluginManager().registerEvents(this.slimeBoss, this);
        this.slimeBoss.runTaskTimer(this, 0L, 10L);
    }

    @Override
    public void onDisable() {
        if (this.slimeBoss != null) {
            this.slimeBoss.destroy();
            this.slimeBoss = null;
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public SlimeBoss getSlimeBoss() {
        return this.slimeBoss;
    }
}