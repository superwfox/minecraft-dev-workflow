package com.tahai.kuangqu;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MineListener implements Listener {
    private final MineManager mineManager;
    private final Map<UUID, MineManager.Mine> playerMines = new HashMap<>();

    public MineListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        MineManager.Mine mine = mineManager.findMineByLocation(loc);
        if (mine == null) return;

        Material type = event.getBlock().getType();
        if (type != Material.DIAMOND_ORE && type != Material.DEEPSLATE_DIAMOND_ORE) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.AQUA + "你不能破坏非钻石矿石！");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        MineManager.Mine mine = mineManager.findMineByLocation(loc);
        if (mine == null) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.AQUA + "你不能在矿区放置方块！");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        MineManager.Mine damagerMine = mineManager.findMineByLocation(damager.getLocation());
        MineManager.Mine victimMine = mineManager.findMineByLocation(victim.getLocation());
        if (damagerMine == null || victimMine == null) return;
        if (!damagerMine.getName().equals(victimMine.getName())) return;

        event.setCancelled(true);
        damager.sendMessage(ChatColor.AQUA + "矿区内禁止PVP！");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        MineManager.Mine fromMine = mineManager.findMineByLocation(from);
        MineManager.Mine toMine = mineManager.findMineByLocation(to);

        UUID uuid = player.getUniqueId();
        MineManager.Mine current = playerMines.get(uuid);

        if (fromMine == null && toMine != null) {
            playerMines.put(uuid, toMine);
            player.sendMessage(ChatColor.GRAY + "你进入了矿区 " + toMine.getName());
        } else if (fromMine != null && toMine == null) {
            playerMines.remove(uuid);
            player.sendMessage(ChatColor.GRAY + "你离开了矿区 " + fromMine.getName());
        } else if (fromMine != null && toMine != null && !fromMine.getName().equals(toMine.getName())) {
            playerMines.put(uuid, toMine);
            player.sendMessage(ChatColor.GRAY + "你离开了矿区 " + fromMine.getName());
            player.sendMessage(ChatColor.GRAY + "你进入了矿区 " + toMine.getName());
        }
    }
}