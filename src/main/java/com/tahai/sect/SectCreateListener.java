package com.tahai.sect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SectCreateListener implements Listener {

    private final DataManager dataManager;
    private final Map<UUID, CreationSession> sessions = new HashMap<>();

    public SectCreateListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void setPendingCreation(Player player, String sectName) {
        sessions.put(player.getUniqueId(), new CreationSession(sectName));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType() != Material.GRASS_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Player player = event.getPlayer();
        CreationSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        Location loc = clicked.getLocation();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            session.pointA = loc;
            player.sendMessage(ChatColor.YELLOW + "已设置宗门领地边界点 A：" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            return;
        }

        if (session.pointA == null) {
            player.sendMessage(ChatColor.AQUA + "请先左键点击设置边界点 A。");
            return;
        }

        if (!loc.getWorld().equals(session.pointA.getWorld())) {
            player.sendMessage(ChatColor.AQUA + "边界点 B 必须与边界点 A 位于同一世界。");
            return;
        }

        session.pointB = loc;
        player.sendMessage(ChatColor.YELLOW + "已设置宗门领地边界点 B：" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());

        String sectName = session.sectName;
        SectClan clan = dataManager.createSect(sectName, player.getUniqueId(), loc.getWorld().getName());
        sessions.remove(player.getUniqueId());

        if (clan == null) {
            player.sendMessage(ChatColor.AQUA + "宗门创建失败，名称可能已被占用。");
            return;
        }

        dataManager.save();
        player.sendMessage(ChatColor.YELLOW + "宗门 " + ChatColor.BOLD + sectName + ChatColor.YELLOW + " 创建成功！");
    }

    private static class CreationSession {
        private final String sectName;
        private Location pointA;
        private Location pointB;

        private CreationSession(String sectName) {
            this.sectName = sectName;
        }
    }
}