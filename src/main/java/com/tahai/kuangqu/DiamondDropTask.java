package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class DiamondDropTask extends BukkitRunnable {

    private final DataManager dataManager;
    private final Random random = new Random();

    public DiamondDropTask(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        ConfigurationSection zones = dataManager.getAllZones();
        if (zones == null) return;

        for (String zoneName : zones.getKeys(false)) {
            ConfigurationSection zone = zones.getConfigurationSection(zoneName);
            if (zone == null) continue;

            String worldName = zone.getString("world");
            if (worldName == null) continue;

            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            int x1 = zone.getInt("x1");
            int y1 = zone.getInt("y1"); // not used for surface
            int z1 = zone.getInt("z1");
            int x2 = zone.getInt("x2");
            int y2 = zone.getInt("y2"); // not used for surface
            int z2 = zone.getInt("z2");

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            int randX = minX + random.nextInt(maxX - minX + 1);
            int randZ = minZ + random.nextInt(maxZ - minZ + 1);

            int surfaceY = world.getHighestBlockYAt(randX, randZ);
            Location dropLoc = new Location(world, randX + 0.5, surfaceY + 1, randZ + 0.5);

            world.dropItem(dropLoc, new ItemStack(Material.DIAMOND));
        }
    }
}