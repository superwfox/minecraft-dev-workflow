package com.tahai.sect;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class SectCommand implements CommandExecutor, TabCompleter {

    private final DataManager dataManager;
    private final SectCreateListener createListener;

    public SectCommand(DataManager dataManager, SectCreateListener createListener) {
        this.dataManager = dataManager;
        this.createListener = createListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "用法: /sect create <名字> | /sect gui");
            return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
            return handleCreate(sender, args);
        }
        if (args[0].equalsIgnoreCase("gui")) {
            return handleGui(sender);
        }
        sender.sendMessage(ChatColor.GRAY + "未知子命令，可用: create、gui");
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("sect.create")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "用法: /sect create <名字>");
            return true;
        }

        Player player = (Player) sender;
        String name = args[1];
        if (name.isEmpty() || name.length() > 20) {
            player.sendMessage(ChatColor.AQUA + "宗门名称长度须为 1-20 个字符。");
            return true;
        }
        if (dataManager.getSect(name) != null) {
            player.sendMessage(ChatColor.AQUA + "已存在同名宗门。");
            return true;
        }
        if (!selectionPrecheck(player)) {
            return true;
        }
        if (!withdrawCreateCost(player)) {
            return true;
        }

        createListener.setPendingCreation(player, name);
        player.sendMessage(ChatColor.YELLOW + "已扣除创建费用，开始选取宗门领地。");
        player.sendMessage(ChatColor.GRAY + "手持草方块右键两个方块设置边界点。");
        return true;
    }

    private boolean handleGui(CommandSender sender) {
        if (!sender.hasPermission("sect.gui")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return true;
        }

        Player player = (Player) sender;
        String clanName = getPlayerClanName(player);
        if (clanName == null) {
            new SectGui(player, SectGui.GuiType.JOIN).open(player);
        } else {
            new SectGui(player, SectGui.GuiType.MANAGE, clanName).open(player);
        }
        return true;
    }

    private boolean selectionPrecheck(Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            player.sendMessage(ChatColor.AQUA + "插件未加载。");
            return false;
        }

        String allowedWorld = plugin.getConfig().getString("world");
        if (allowedWorld != null && !allowedWorld.isEmpty() && !player.getWorld().getName().equals(allowedWorld)) {
            player.sendMessage(ChatColor.AQUA + "只能在 " + allowedWorld + " 世界创建宗门。");
            return false;
        }
        if (!player.getInventory().contains(Material.GRASS_BLOCK)) {
            player.sendMessage(ChatColor.AQUA + "请先准备一个草方块，用于选择领地边界点。");
            return false;
        }
        return true;
    }

    private boolean withdrawCreateCost(Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        double cost = plugin == null ? 1000.0 : plugin.getConfig().getDouble("cost", 1000.0);
        if (cost <= 0) {
            return true;
        }

        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            player.sendMessage(ChatColor.AQUA + "服务器未安装 Vault 经济插件。");
            return false;
        }
        Economy economy = provider.getProvider();
        if (economy == null) {
            player.sendMessage(ChatColor.AQUA + "服务器未安装有效的经济插件。");
            return false;
        }
        if (economy.getBalance(player) < cost) {
            player.sendMessage(ChatColor.AQUA + "余额不足，需要 " + economy.format(cost));
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, cost);
        if (!response.transactionSuccess()) {
            player.sendMessage(ChatColor.AQUA + "扣除创建费用失败: " + response.errorMessage);
            return false;
        }
        return true;
    }

    private String getPlayerClanName(Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            return null;
        }

        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        if (config.getConfigurationSection("clans") == null) {
            return null;
        }

        String uuid = player.getUniqueId().toString();
        for (String clanName : config.getConfigurationSection("clans").getKeys(false)) {
            String base = "clans." + clanName;
            if (uuid.equals(config.getString(base + ".leader"))) {
                return clanName;
            }
            if (config.contains(base + ".members." + uuid)) {
                return clanName;
            }
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("sect.create") && "create".startsWith(prefix)) {
                completions.add("create");
            }
            if (sender.hasPermission("sect.gui") && "gui".startsWith(prefix)) {
                completions.add("gui");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}