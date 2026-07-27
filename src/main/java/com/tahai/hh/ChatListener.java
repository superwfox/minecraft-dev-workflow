package com.tahai.hh;

import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ChatListener implements Listener {

    private final Plugin plugin;

    public ChatListener() {
        this.plugin = Bukkit.getPluginManager().getPlugin("hh");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (plugin == null) return;

        SessionManager sessionManager = getSessionManager();
        if (sessionManager == null || !sessionManager.isPlayerActive(player.getUniqueId())) return;

        event.setCancelled(true);
        String input = event.getMessage().trim();
        int option;
        try {
            option = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            String template = plugin.getConfig().getString("messages.invalid", "输入无效，请输入数字。");
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                template = PlaceholderAPI.setPlaceholders(player, template);
            }
            player.sendMessage(ChatColor.AQUA + ChatColor.stripColor(template));
            reopenMenu(player);
            return;
        }

        ConfigurationSection items = plugin.getConfig().getConfigurationSection("items");
        if (items == null || !items.contains(String.valueOf(option))) {
            String template = plugin.getConfig().getString("messages.invalid", "无效的选项。");
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                template = PlaceholderAPI.setPlaceholders(player, template);
            }
            player.sendMessage(ChatColor.AQUA + ChatColor.stripColor(template));
            reopenMenu(player);
            return;
        }

        ConfigurationSection item = items.getConfigurationSection(String.valueOf(option));
        double amount = item.getDouble("amount", 0);
        int points = item.getInt("points", 0);

        Economy economy = getEconomy();
        if (economy == null) {
            player.sendMessage(ChatColor.AQUA + "经济系统不可用");
            sessionManager.removePlayer(player.getUniqueId());
            return;
        }

        double balance = economy.getBalance(player);
        if (balance < amount) {
            String template = plugin.getConfig().getString("messages.not_enough", "金币不足，需要{amount}金币。");
            template = template.replace("{amount}", String.valueOf((int) amount));
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                template = PlaceholderAPI.setPlaceholders(player, template);
            }
            player.sendMessage(ChatColor.AQUA + ChatColor.stripColor(template));
            reopenMenu(player);
            return;
        }

        // 扣除金币
        economy.withdrawPlayer(player, amount);

        // 增加点券
        PlayerPoints playerPoints = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (playerPoints != null) {
            playerPoints.getAPI().give(player.getUniqueId(), points);
        }

        String template = plugin.getConfig().getString("messages.success", "成功兑换{amount}金币为{points}点券！");
        template = template.replace("{amount}", String.valueOf((int) amount));
        template = template.replace("{points}", String.valueOf(points));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            template = PlaceholderAPI.setPlaceholders(player, template);
        }
        player.sendMessage(ChatColor.YELLOW + ChatColor.stripColor(template));

        sessionManager.removePlayer(player.getUniqueId());
    }

    private SessionManager getSessionManager() {
        RegisteredServiceProvider<SessionManager> rsp = Bukkit.getServicesManager().getRegistration(SessionManager.class);
        return rsp != null ? rsp.getProvider() : null;
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        return rsp != null ? rsp.getProvider() : null;
    }

    private void reopenMenu(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> player.performCommand("hh"));
    }
}