package com.tahai.hs;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RecycleGui implements InventoryHolder {
    public static final int BUTTON_SLOT = 22;
    public static final int GUI_SIZE = 27;
    private final Inventory inventory;
    private final Plugin plugin;

    public RecycleGui(Player player, Plugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, GUI_SIZE, ChatColor.GREEN + "回收商店");
        setupButton();
        player.openInventory(inventory);
    }

    private void setupButton() {
        ItemStack button = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "点击回收");
        button.setItemMeta(meta);
        inventory.setItem(BUTTON_SLOT, button);
    }

    public void recycle(Player player) {
        ConfigurationSection pricesSection = plugin.getConfig().getConfigurationSection("prices");
        if (pricesSection == null) {
            player.sendMessage(ChatColor.RED + "配置错误：缺少价格配置。");
            return;
        }

        String noRecycleMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("no-recycle-message", "&7背包中没有可回收的物品。"));
        String successMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("success-message", "&e成功回收 %amount% 个物品，获得 %total_gold% 金币。"));

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            player.sendMessage(ChatColor.RED + "经济系统未安装。");
            return;
        }
        Economy economy = rsp.getProvider();

        int totalAmount = 0;
        double totalGold = 0.0;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == BUTTON_SLOT) continue;
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            String matName = item.getType().name();
            if (pricesSection.contains(matName)) {
                double price = pricesSection.getDouble(matName);
                int amount = item.getAmount();
                totalAmount += amount;
                totalGold += price * amount;
                inventory.setItem(i, null);
            }
        }

        if (totalAmount == 0) {
            player.sendMessage(noRecycleMsg);
        } else {
            economy.depositPlayer(player, totalGold);

            if (plugin instanceof Main) {
                StatsManager statsManager = ((Main) plugin).getStatsManager();
                if (statsManager != null) {
                    statsManager.addRecycle(player, totalAmount, totalGold);
                }
            }

            String msg = successMsg
                    .replace("%amount%", String.valueOf(totalAmount))
                    .replace("%total_gold%", String.format("%.2f", totalGold));
            player.sendMessage(msg);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}