package com.example.landplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EventListener implements Listener {

    private final LandManager landManager;

    public EventListener(LandManager landManager) {
        this.landManager = landManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;
        Block block = event.getBlock();
        if (!isProtected(block.getLocation(), player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;
        Block block = event.getBlock();
        if (!isProtected(block.getLocation(), player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        if (!isProtected(block.getLocation(), player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (player.isOp()) return;
        Location loc = event.getEntity().getLocation();
        if (!isProtected(loc, player)) {
            event.setCancelled(true);
        }
    }

    private boolean isProtected(Location location, Player player) {
        LandData land = landManager.getLand(location);
        if (land == null) return true;
        if (land.isOwner(player.getUniqueId())) return true;
        if (land.isTrusted(player.getUniqueId())) return true;
        return false;
    }
}