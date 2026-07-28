package com.tahai.kuangqu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class KqCommand implements CommandExecutor, TabCompleter {

    private final SelectionManager selectionManager;
    private final DataManager dataManager;

    public KqCommand(SelectionManager selectionManager, DataManager dataManager) {
        this.selectionManager = selectionManager;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("kq.qx.use")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限使用此命令");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.GRAY + "用法: /kq <create|reload>");
            return true;
        }

        String sub = args[0].toLowerCase();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Kuangqu");
        if (plugin == null) {
            player.sendMessage(ChatColor.AQUA + "无法找到插件实例");
            return true;
        }

        switch (sub) {
            case "create":
                if (!player.hasPermission("kq.qx.create")) {
                    player.sendMessage(ChatColor.AQUA + "你没有权限创建矿区");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.GRAY + "用法: /kq create <名称>");
                    return true;
                }
                String zoneName = args[1];
                if (selectionManager == null) {
                    player.sendMessage(ChatColor.AQUA + "无法获取选区管理器");
                    return true;
                }
                Selection selection = selectionManager.getSelection(player.getUniqueId());
                if (selection == null || selection.getPos1() == null || selection.getPos2() == null) {
                    player.sendMessage(ChatColor.AQUA + "请先使用木锹选择两个点");
                    return true;
                }
                Location pos1 = selection.getPos1();
                Location pos2 = selection.getPos2();

                Map<String, Object> data = new HashMap<>();
                data.put("world", pos1.getWorld().getName());
                data.put("x1", Math.min(pos1.getBlockX(), pos2.getBlockX()));
                data.put("y1", Math.min(pos1.getBlockY(), pos2.getBlockY()));
                data.put("z1", Math.min(pos1.getBlockZ(), pos2.getBlockZ()));
                data.put("x2", Math.max(pos1.getBlockX(), pos2.getBlockX()));
                data.put("y2", Math.max(pos1.getBlockY(), pos2.getBlockY()));
                data.put("z2", Math.max(pos1.getBlockZ(), pos2.getBlockZ()));
                data.put("fill-material", "STONE");
                data.put("ore-distribution", new HashMap<String, Integer>());

                if (dataManager == null) {
                    player.sendMessage(ChatColor.AQUA + "无法获取数据管理器");
                    return true;
                }
                dataManager.addZone(zoneName, data);
                dataManager.saveConfig();

                World world = pos1.getWorld();
                String regionId = "kq_" + zoneName;
                if (WorldGuardUtil.isWorldGuardLoaded()) {
                    boolean success = WorldGuardUtil.createProtectedRegion(world, regionId, pos1, pos2, Collections.emptyList());
                    if (!success) {
                        player.sendMessage(ChatColor.AQUA + "WorldGuard保护区创建失败");
                    }
                } else {
                    player.sendMessage(ChatColor.GRAY + "WorldGuard未加载，跳过保护区创建");
                }

                player.sendMessage(ChatColor.YELLOW + "矿区 " + zoneName + " 创建成功！");
                break;
            case "reload":
                if (!player.hasPermission("kq.qx.reload")) {
                    player.sendMessage(ChatColor.AQUA + "你没有权限重载配置");
                    return true;
                }
                if (dataManager == null) {
                    player.sendMessage(ChatColor.AQUA + "无法获取数据管理器");
                    return true;
                }
                dataManager.reloadConfig();
                player.sendMessage(ChatColor.YELLOW + "配置已重载");
                break;
            default:
                player.sendMessage(ChatColor.GRAY + "未知子命令，可用: create, reload");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}