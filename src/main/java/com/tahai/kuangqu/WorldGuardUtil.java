package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Collection;
import java.util.List;

public final class WorldGuardUtil {

    private WorldGuardUtil() {}

    public static boolean isWorldGuardLoaded() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public static boolean createProtectedRegion(World world, String regionId, Location pos1, Location pos2, Collection<String> disabledCommands) {
        if (!isWorldGuardLoaded()) {
            Bukkit.getLogger().warning("[Kuangqu] WorldGuard not loaded, cannot create region.");
            return false;
        }
        try {
            RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (manager == null) {
                Bukkit.getLogger().warning("[Kuangqu] RegionManager is null for world " + world.getName());
                return false;
            }
            if (manager.hasRegion(regionId)) {
                Bukkit.getLogger().warning("[Kuangqu] Region " + regionId + " already exists.");
                return false;
            }
            BlockVector3 min = BlockVector3.at(
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ())
            );
            BlockVector3 max = BlockVector3.at(
                Math.max(pos1.getBlockX(), pos2.getBlockX()),
                Math.max(pos1.getBlockY(), pos2.getBlockY()),
                Math.max(pos1.getBlockZ(), pos2.getBlockZ())
            );
            ProtectedRegion region = new ProtectedCuboidRegion(regionId, min, max);
            region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
            region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
            if (disabledCommands != null && !disabledCommands.isEmpty()) {
                region.setFlag(Flags.DISALLOWED_COMMANDS, List.copyOf(disabledCommands));
            }
            manager.addRegion(region);
            manager.saveChanges();
            Bukkit.getLogger().info("[Kuangqu] Region " + regionId + " created successfully.");
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[Kuangqu] Failed to create region " + regionId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteRegion(World world, String regionId) {
        if (!isWorldGuardLoaded()) {
            Bukkit.getLogger().warning("[Kuangqu] WorldGuard not loaded, cannot delete region.");
            return false;
        }
        try {
            RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (manager == null) {
                Bukkit.getLogger().warning("[Kuangqu] RegionManager is null for world " + world.getName());
                return false;
            }
            if (!manager.hasRegion(regionId)) {
                Bukkit.getLogger().warning("[Kuangqu] Region " + regionId + " does not exist.");
                return false;
            }
            manager.removeRegion(regionId);
            manager.saveChanges();
            Bukkit.getLogger().info("[Kuangqu] Region " + regionId + " deleted successfully.");
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[Kuangqu] Failed to delete region " + regionId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}