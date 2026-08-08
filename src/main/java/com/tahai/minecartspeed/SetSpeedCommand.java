package com.tahai.minecartspeed;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SetSpeedCommand implements CommandExecutor, TabCompleter {

    private final SelectionManager selectionManager;
    private final RegionManager regionManager;

    public SetSpeedCommand(SelectionManager selectionManager, RegionManager regionManager) {
        this.selectionManager = selectionManager;
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("minecartspeed.speedset")) {
            sender.sendMessage(ChatColor.GRAY + "你没有权限执行此命令。");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.GRAY + "该命令只能由玩家执行。");
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("set")) {
            sender.sendMessage(ChatColor.GRAY + "用法: /minecartspeed set <速度>");
            return true;
        }
        double speed;
        try {
            speed = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.AQUA + "请输入有效数字。");
            return true;
        }
        Plugin rawPlugin = Bukkit.getPluginManager().getPlugin("MinecartSpeed");
        if (rawPlugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未加载。");
            return true;
        }
        if (!(rawPlugin instanceof JavaPlugin plugin)) {
            sender.sendMessage(ChatColor.AQUA + "插件类型错误。");
            return true;
        }
        double minSpeed = plugin.getConfig().getDouble("min-speed", 0.4);
        double maxSpeed = plugin.getConfig().getDouble("max-speed", 8.0);
        if (speed < minSpeed || speed > maxSpeed) {
            sender.sendMessage(ChatColor.AQUA + "速度必须介于 " + minSpeed + " 和 " + maxSpeed + " 之间。");
            return true;
        }
        if (!selectionManager.isComplete(player.getUniqueId())) {
            sender.sendMessage(ChatColor.AQUA + "请先用木棍选择两个点。");
            return true;
        }
        Location[] points = selectionManager.getPoints(player.getUniqueId());
        Location loc1 = points[0];
        Location loc2 = points[1];
        World world = player.getWorld();
        regionManager.addRegion(world, loc1, loc2, speed);

        String enterTemplate = plugin.getConfig().getString("messages.enter", "&e你已进入限速区域：%speed%");
        String enterMessage = ChatColor.translateAlternateColorCodes('&', enterTemplate)
                .replace("%speed%", String.valueOf(speed));
        int updated = updateMinecarts(world, loc1, loc2, speed, enterMessage);
        sender.sendMessage(ChatColor.YELLOW + "已设置限速区域，速度: " + speed + "，已更新 " + updated + " 辆矿车。");
        return true;
    }

    private int updateMinecarts(World world, Location loc1, Location loc2, double speed, String enterMessage) {
        double minX = Math.min(loc1.getX(), loc2.getX());
        double maxX = Math.max(loc1.getX(), loc2.getX());
        double minY = Math.min(loc1.getY(), loc2.getY());
        double maxY = Math.max(loc1.getY(), loc2.getY());
        double minZ = Math.min(loc1.getZ(), loc2.getZ());
        double maxZ = Math.max(loc1.getZ(), loc2.getZ());

        int updated = 0;
        for (Minecart minecart : world.getEntitiesByClass(Minecart.class)) {
            Location loc = minecart.getLocation();
            if (loc.getX() >= minX && loc.getX() <= maxX
                    && loc.getY() >= minY && loc.getY() <= maxY
                    && loc.getZ() >= minZ && loc.getZ() <= maxZ) {
                minecart.setMaxSpeed(speed);
                for (Entity passenger : minecart.getPassengers()) {
                    if (passenger instanceof Player player) {
                        player.sendActionBar(enterMessage);
                    }
                }
                updated++;
            }
        }
        return updated;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if ("set".startsWith(args[0].toLowerCase())) {
                return List.of("set");
            }
        }
        return Collections.emptyList();
    }
}