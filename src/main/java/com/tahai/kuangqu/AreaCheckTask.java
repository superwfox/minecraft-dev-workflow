package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AreaCheckTask extends BukkitRunnable {

    private final DataManager dataManager;
    private final Map<UUID, String> playerZones = new HashMap<>();

    public AreaCheckTask(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * 获取玩家当前所在的矿区名称，不在任何矿区则返回 null。
     */
    public String getPlayerCurrentZone(UUID playerId) {
        return playerZones.get(playerId);
    }

    @Override
    public void run() {
        ConfigurationSection zonesSection = dataManager.getAllZones();
        if (zonesSection == null) return;

        String enterMsgRaw = dataManager.getConfig().getString("messages.enter", "§7您已进入 §e{mine} §7矿区");
        String leaveMsgRaw = dataManager.getConfig().getString("messages.leave", "§7您已离开 §e{mine} §7矿区");

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Location loc = player.getLocation();
            String currentZone = null;

            for (String zoneName : zonesSection.getKeys(false)) {
                ConfigurationSection zone = dataManager.getZone(zoneName);
                if (zone == null) continue;

                String worldName = zone.getString("world");
                if (worldName == null) continue;
                World world = Bukkit.getWorld(worldName);
                if (world == null || !world.equals(loc.getWorld())) continue;

                // 读取区域边界（坐标可以为任意顺序）
                double x1 = zone.getDouble("x1");
                double y1 = zone.getDouble("y1");
                double z1 = zone.getDouble("z1");
                double x2 = zone.getDouble("x2");
                double y2 = zone.getDouble("y2");
                double z2 = zone.getDouble("z2");

                double minX = Math.min(x1, x2);
                double maxX = Math.max(x1, x2);
                double minY = Math.min(y1, y2);
                double maxY = Math.max(y1, y2);
                double minZ = Math.min(z1, z2);
                double maxZ = Math.max(z1, z2);

                if (loc.getX() >= minX && loc.getX() <= maxX &&
                    loc.getY() >= minY && loc.getY() <= maxY &&
                    loc.getZ() >= minZ && loc.getZ() <= maxZ) {
                    currentZone = zoneName;
                    break;
                }
            }

            String oldZone = playerZones.get(uuid);

            if (currentZone == null && oldZone != null) {
                // 离开矿区
                String leaveMsg = ChatColor.translateAlternateColorCodes('&',
                        leaveMsgRaw.replace("{mine}", oldZone));
                player.sendMessage(leaveMsg);
                playerZones.remove(uuid);
            } else if (currentZone != null && !currentZone.equals(oldZone)) {
                // 进入新矿区（可能之前也在另一个区）
                if (oldZone != null) {
                    String leaveMsg = ChatColor.translateAlternateColorCodes('&',
                            leaveMsgRaw.replace("{mine}", oldZone));
                    player.sendMessage(leaveMsg);
                }
                String enterMsg = ChatColor.translateAlternateColorCodes('&',
                        enterMsgRaw.replace("{mine}", currentZone));
                player.sendMessage(enterMsg);
                playerZones.put(uuid, currentZone);
            }
        }
    }
}