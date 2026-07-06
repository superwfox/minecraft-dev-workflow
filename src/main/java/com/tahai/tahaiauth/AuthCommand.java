package com.tahai.tahaiauth;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthCommand implements CommandExecutor, TabCompleter {

    private final DataManager dataManager;

    public AuthCommand() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("TahaiAuth");
        if (plugin == null) {
            throw new IllegalStateException("TahaiAuth plugin not loaded");
        }
        this.dataManager = new DataManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tahaiauth.use")) {
            sender.sendMessage("§c你没有权限使用此命令");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§c用法: /auth <register|login|admin>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "register":
                return handleRegister(sender, args);
            case "login":
                return handleLogin(sender, args);
            case "admin":
                return handleAdmin(sender, args);
            default:
                sender.sendMessage("§c未知子命令: " + sub);
                return true;
        }
    }

    private boolean handleRegister(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以注册");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§c用法: /auth register <密码>");
            return true;
        }
        UUID uuid = player.getUniqueId();
        if (dataManager.isRegistered(uuid)) {
            sender.sendMessage("§c你已经注册过了");
            return true;
        }
        boolean success = dataManager.registerPlayer(uuid, args[1]);
        if (success) {
            sender.sendMessage("§a注册成功！请使用 /auth login 登录");
        } else {
            sender.sendMessage("§c注册失败，请重试");
        }
        return true;
    }

    private boolean handleLogin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以登录");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§c用法: /auth login <密码>");
            return true;
        }
        UUID uuid = player.getUniqueId();
        if (!dataManager.isRegistered(uuid)) {
            sender.sendMessage("§c你还没有注册，请先使用 /auth register <密码> 注册");
            return true;
        }
        if (dataManager.isAuthenticated(uuid)) {
            sender.sendMessage("§a你已经登录了");
            return true;
        }
        boolean valid = dataManager.verifyPlayer(uuid, args[1]);
        if (valid) {
            dataManager.setAuthenticated(uuid, true);
            dataManager.updateLastActive(uuid);
            sender.sendMessage("§a登录成功！");
        } else {
            sender.sendMessage("§c密码错误");
        }
        return true;
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tahaiauth.admin")) {
            sender.sendMessage("§c你没有管理员权限");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§c用法: /auth admin <list|reset>");
            return true;
        }
        String subAdmin = args[1].toLowerCase();
        switch (subAdmin) {
            case "list":
                return handleAdminList(sender);
            case "reset":
                return handleAdminReset(sender, args);
            default:
                sender.sendMessage("§c未知管理子命令: " + subAdmin);
                return true;
        }
    }

    private boolean handleAdminList(CommandSender sender) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("TahaiAuth");
        if (plugin == null) return true;
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            sender.sendMessage("§c数据文件不存在");
            return true;
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = data.getConfigurationSection("");
        if (section == null || section.getKeys(false).isEmpty()) {
            sender.sendMessage("§e没有已注册的玩家");
            return true;
        }
        sender.sendMessage("§6已注册的玩家密码密文:");
        for (String key : section.getKeys(false)) {
            String hash = data.getString(key + ".hash", "未知");
            sender.sendMessage("§7" + key + " §f-> §e" + hash);
        }
        return true;
    }

    private boolean handleAdminReset(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§c用法: /auth admin reset <玩家名> <新密码>");
            return true;
        }
        String playerName = args[2];
        String newPassword = args[3];
        UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        if (!dataManager.isRegistered(uuid)) {
            sender.sendMessage("§c该玩家没有注册");
            return true;
        }
        dataManager.resetPassword(uuid, newPassword);
        sender.sendMessage("§a已重置玩家 " + playerName + " 的密码");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("register");
            completions.add("login");
            if (sender.hasPermission("tahaiauth.admin")) {
                completions.add("admin");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("tahaiauth.admin")) {
                completions.add("list");
                completions.add("reset");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("reset") && sender.hasPermission("tahaiauth.admin")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            }
        }
        return completions;
    }
}