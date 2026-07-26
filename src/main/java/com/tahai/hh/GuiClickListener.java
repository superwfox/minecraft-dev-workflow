package com.tahai.hh;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.api.PlayerPointsAPI;

public class GuiClickListener implements Listener {

    private final NamespacedKey costKey;
    private final NamespacedKey rewardKey;

    public GuiClickListener() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Hh");
        this.costKey = new NamespacedKey(plugin, "cost");
        this.rewardKey = new NamespacedKey(plugin, "reward");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof GuiHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(costKey, PersistentDataType.INTEGER) || !pdc.has(rewardKey, PersistentDataType.INTEGER)) {
            return;
        }

        int cost = pdc.get(costKey, PersistentDataType.INTEGER);
        int reward = pdc.get(rewardKey, PersistentDataType.INTEGER);

        // Vault Economy
        RegisteredServiceProvider<Economy> econProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (econProvider == null) {
            player.sendMessage(ChatColor.AQUA + "经济系统未加载！");
            player.closeInventory();
            return;
        }
        Economy econ = econProvider.getProvider();

        // PlayerPoints API
        RegisteredServiceProvider<PlayerPointsAPI> ppProvider = Bukkit.getServicesManager().getRegistration(PlayerPointsAPI.class);
        if (ppProvider == null) {
            player.sendMessage(ChatColor.AQUA + "点券系统未加载！");
            player.closeInventory();
            return;
        }
        PlayerPointsAPI pointsAPI = ppProvider.getProvider();

        if (econ.has(player, cost)) {
            econ.withdrawPlayer(player, cost);
            pointsAPI.give(player.getUniqueId(), reward);
            player.sendMessage(ChatColor.YELLOW + "成功兑换 " + reward + " 点券！");
        } else {
            player.sendMessage(ChatColor.AQUA + "金币不足！");
        }

        player.closeInventory();
    }
}