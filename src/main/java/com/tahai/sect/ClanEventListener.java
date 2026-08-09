package com.tahai.sect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanEventListener implements Listener {

    private final ClanManager clanManager;
    private final Map<UUID, Location[]> points = new HashMap<>();

    public ClanEventListener(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null || event.getItem().getType() != Material.GRASS_BLOCK) return;
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        Location loc = event.getClickedBlock().getLocation();
        UUID id = player.getUniqueId();
        points.computeIfAbsent(id, k -> new Location[2]);
        boolean a = action == Action.LEFT_CLICK_BLOCK;
        points.get(id)[a ? 0 : 1] = loc.clone();
        player.sendMessage(ChatColor.GRAY + (a ? "A" : "B") + "点已设置: "
                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity() instanceof Player victim) {
            if (victim.getKiller() instanceof Player killer) {
                clanManager.onPlayerKill(killer, victim);
            }
        }
    }
}