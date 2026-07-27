package com.tahai.hh;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiListener implements Listener {

    private static final Pattern PATTERN = Pattern.compile("兑换\\s*(\\d+)\\s*金币\\s*[->→]\\s*(\\d+)\\s*点券");

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ExchangeGui)) {
            return;
        }
        event.setCancelled(true);

        if (event.getRawSlot() >= event.getInventory().getSize()) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Matcher matcher = PATTERN.matcher(displayName);
        if (!matcher.matches()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int gold;
        int points;
        try {
            gold = Integer.parseInt(matcher.group(1));
            points = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException e) {
            return;
        }

        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        Plugin playerPointsPlugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (vaultPlugin == null || playerPointsPlugin == null) {
            player.sendMessage(ChatColor.AQUA + "经济系统未正确加载。");
            player.closeInventory();
            return;
        }

        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        if (economy == null) {
            player.sendMessage(ChatColor.AQUA + "Vault经济未注册。");
            player.closeInventory();
            return;
        }

        if (economy.has(player, gold)) {
            economy.withdrawPlayer(player, gold);
            PlayerPointsAPI api = ((PlayerPoints) playerPointsPlugin).getAPI();
            api.give(player.getUniqueId(), points);
            player.sendMessage(ChatColor.YELLOW + "兑换成功！消耗了 " + gold + " 金币，获得了 " + points + " 点券。");
        } else {
            double balance = economy.getBalance(player);
            player.sendMessage(ChatColor.AQUA + "你的金币不足！需要 " + gold + " 金币，你只有 " + (int)balance + " 金币。");
        }

        player.closeInventory();
    }
}