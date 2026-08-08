package com.tahai.minecartspeed;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class SelectionListener implements Listener {
    private final Map<UUID, double[]> firstPoints = new HashMap<>();
    private final Map<UUID, double[]> secondPoints = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.STICK) return;
        if (!player.hasPermission("minecartspeed.select")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限使用选区工具。");
            return;
        }

        if (event.getClickedBlock() == null) {
            player.sendMessage(ChatColor.AQUA + "你必须点击一个方块来设置点。");
            return;
        }

        Location loc = event.getClickedBlock().getLocation();
        double[] point = {loc.getX(), loc.getY(), loc.getZ()};

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            firstPoints.put(player.getUniqueId(), point);
            player.sendMessage(ChatColor.YELLOW + "第一点已设置: " + format(point));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!firstPoints.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.AQUA + "请先左键设置第一点。");
                return;
            }
            secondPoints.put(player.getUniqueId(), point);
            player.sendMessage(ChatColor.YELLOW + "第二点已设置: " + format(point));
            player.sendMessage(ChatColor.GRAY + "选区完成，可以使用 /minecartspeed 命令来管理限速区域。");
        }
    }

    private String format(double[] point) {
        return String.format("%.1f, %.1f, %.1f", point[0], point[1], point[2]);
    }
}