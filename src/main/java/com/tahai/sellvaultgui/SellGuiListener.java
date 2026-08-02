package com.tahai.sellvaultgui;

import java.io.File;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class SellGuiListener implements Listener {
    private final PriceManager priceManager;
    private final YamlConfiguration messages;

    public SellGuiListener(Plugin plugin, PriceManager priceManager) {
        this.priceManager = priceManager;
        this.messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SellGui)) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() == 22) {
            handleSell(event);
        }
    }

    private void handleSell(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();

        double total = 0;
        int count = 0;

        for (int i = 0; i < inv.getSize(); i++) {
            if (i == 22) continue;
            ItemStack item = inv.getItem(i);
            if (item != null) {
                total += priceManager.getPrice(item);
                count += item.getAmount();
            }
        }

        Economy economy = getEconomy();
        if (economy == null) {
            player.sendMessage(getMessage("sell-vault-not-found"));
            return;
        }

        EconomyResponse response = economy.depositPlayer(player, total);
        if (!response.transactionSuccess()) {
            player.sendMessage(getMessage("sell-error", "%amount%", String.valueOf(count), "%money%", economy.format(total)));
            return;
        }

        player.sendMessage(getMessage("sell-success", "%amount%", String.valueOf(count), "%money%", economy.format(total)));

        for (int i = 0; i < inv.getSize(); i++) {
            if (i == 22) continue;
            inv.setItem(i, null);
        }
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : rsp.getProvider();
    }

    private String getMessage(String key, String... replacements) {
        String msg = messages.getString(key);
        if (msg == null) {
            msg = getDefault(key);
        }

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private String getDefault(String key) {
        switch (key) {
            case "sell-success":
                return ChatColor.YELLOW + "成功出售了 %amount% 个物品，获得 %money%！";
            case "sell-vault-not-found":
                return ChatColor.AQUA + "经济系统不可用，无法出售物品。";
            case "sell-error":
                return ChatColor.AQUA + "出售失败，请稍后再试。";
            default:
                return "";
        }
    }
}