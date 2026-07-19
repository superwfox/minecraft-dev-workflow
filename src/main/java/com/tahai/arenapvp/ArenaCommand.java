package com.tahai.arenapvp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArenaCommand implements CommandExecutor, TabCompleter {
    private final ArenaManager arenaManager;
    private final Map<String, List<Location>> gameSpawns = new HashMap<>();
    private final Map<String, List<Location>> gameSpectatePoints = new HashMap<>();
    private final Map<String, Object> kits = new HashMap<>();
    private final File kitFile;
    private final YamlConfiguration kitConfig;

    public ArenaCommand(ArenaManager arenaManager, Plugin plugin) {
        this.arenaManager = arenaManager;
        plugin.getDataFolder().mkdirs();
        kitFile = new File(plugin.getDataFolder(), "kits.yml");
        kitConfig = YamlConfiguration.loadConfiguration(kitFile);
        loadKits();
    }

    private void loadKits() {
        for (String key : kitConfig.getKeys(false)) {
            kits.put(key, kitConfig.get(key));
        }
    }

    private void saveKits() {
        for (Map.Entry<String, Object> entry : kits.entrySet()) {
            kitConfig.set(entry.getKey(), entry.getValue());
        }
        try { kitConfig.save(kitFile); } catch (Exception ignored) {}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("arena")) {
            return handleArenaCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("duel")) {
            return handleDuelCommand(sender, args);
        }
        return false;
    }

    private boolean handleArenaCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("arenapvp.use")) {
            sender.sendMessage(ChatColor.GRAY + "你没有权限执行此命令。");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /arena <子命令>");
            sender.sendMessage(ChatColor.GRAY + "子命令: create, setspawn, setspectate, setlobby, kit, menu");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create": return createGame(sender, args);
            case "setspawn": return setSpawn(sender, args);
            case "setspectate": return setSpectate(sender, args);
            case "setlobby": return setLobby(sender, args);
            case "kit": return manageKit(sender, args);
            case "menu": return openMenu(sender, args);
            default: sender.sendMessage(ChatColor.AQUA + "未知子命令: " + args[0]); return true;
        }
    }

    private boolean createGame(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.GRAY + "只有玩家可以执行此命令。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /arena create <地图名>");
            return true;
        }
        String name = args[1];
        List<Location> spawns = new ArrayList<>();
        arenaManager.createGame(name, player.getWorld(), spawns, 2);
        gameSpawns.put(name, spawns);
        sender.sendMessage(ChatColor.YELLOW + "游戏 " + name + " 已创建。");
        return true;
    }

    private boolean setSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.GRAY + "只有玩家可以执行此命令。");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /arena setspawn <地图名> <编号>");
            return true;
        }
        String name = args[1];
        int index;
        try { index = Integer.parseInt(args[2]); } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.AQUA + "编号必须为数字。"); return true;
        }
        gameSpawns.computeIfAbsent(name, k -> new ArrayList<>());
        List<Location> spawns = gameSpawns.get(name);
        if (index < 0 || index > spawns.size()) {
            sender.sendMessage(ChatColor.AQUA + "编号超出范围。"); return true;
        }
        if (index == spawns.size()) spawns.add(player.getLocation());
        else spawns.set(index, player.getLocation());
        sender.sendMessage(ChatColor.YELLOW + "已设置出生点 " + index);
        return true;
    }

    private boolean setSpectate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.GRAY + "只有玩家可以执行此命令。");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /arena setspectate <地图名> <编号>");
            return true;
        }
        String name = args[1];
        int index;
        try { index = Integer.parseInt(args[2]); } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.AQUA + "编号必须为数字。"); return true;
        }
        gameSpectatePoints.computeIfAbsent(name, k -> new ArrayList<>());
        List<Location> points = gameSpectatePoints.get(name);
        if (index < 0 || index > points.size()) {
            sender.sendMessage(ChatColor.AQUA + "编号超出范围。"); return true;
        }
        if (index == points.size()) points.add(player.getLocation());
        else points.set(index, player.getLocation());
        sender.sendMessage(ChatColor.YELLOW + "已设置旁观点 " + index);
        return true;
    }

    private boolean setLobby(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.GRAY + "只有玩家可以执行此命令。");
            return true;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ArenaPVP");
        if (plugin == null) return true;
        Location loc = player.getLocation();
        plugin.getConfig().set("lobby.world", loc.getWorld().getName());
        plugin.getConfig().set("lobby.x", loc.getX());
        plugin.getConfig().set("lobby.y", loc.getY());
        plugin.getConfig().set("lobby.z", loc.getZ());
        plugin.getConfig().set("lobby.yaw", loc.getYaw());
        plugin.getConfig().set("lobby.pitch", loc.getPitch());
        plugin.saveConfig();
        sender.sendMessage(ChatColor.YELLOW + "主城位置已设置。");
        return true;
    }

    private boolean manageKit(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "Kit 管理未完全实现。");
        return true;
    }

    private boolean openMenu(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.GRAY + "只有玩家可以执行此命令。");
            return true;
        }
        Inventory inv = Bukkit.createInventory(new MenuHolder(), 9, ChatColor.LIGHT_PURPLE + "ArenaPVP 菜单");
        player.openInventory(inv);
        sender.sendMessage(ChatColor.YELLOW + "已打开菜单。");
        return true;
    }

    private boolean handleDuelCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.GRAY + "只有玩家可以执行此命令。");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /duel <玩家名>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.AQUA + "找不到玩家 " + args[0]);
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "你向 " + target.getName() + " 发起了决斗挑战!");
        target.sendMessage(ChatColor.YELLOW + player.getName() + " 向你发起了决斗挑战!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("arena")) {
            if (args.length == 1) {
                return Stream.of("create", "setspawn", "setspectate", "setlobby", "kit", "menu")
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String sub = args[0].toLowerCase();
                if ("setspawn".equals(sub) || "setspectate".equals(sub)) {
                    Collection<Game> games = arenaManager.getGames();
                    return games.stream().map(Game::getName)
                            .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
                return Collections.emptyList();
            } else {
                return Collections.emptyList();
            }
        } else if (command.getName().equalsIgnoreCase("duel")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private static class MenuHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() { return null; }
    }
}