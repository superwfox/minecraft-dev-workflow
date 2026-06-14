package com.example.coppersword;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CoolDownGui implements Listener, InventoryHolder {

    private final Inventory inventory;
    private final Player targetPlayer;

    public CoolDownGui(Player player) {
        this.targetPlayer = player;
        this.inventory = Bukkit.createInventory(this, 9, ChatColor.GOLD + "铜剑冷却状态");
        initializeItems();
    }

    private void initializeItems() {
        CooldownManager cooldownManager = new CooldownManager();
        boolean onCooldown = cooldownManager.isOnCooldown(targetPlayer);

        ItemStack sword = new ItemStack(Material.COPPER_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(ChatColor.BOLD + "铜剑技能");
        if (onCooldown) {
            meta.setLore(Arrays.asList(ChatColor.RED + "状态: 冷却中"));
        } else {
            meta.setLore(Arrays.asList(ChatColor.GREEN + "状态: 可用"));
        }
        sword.setItemMeta(meta);

        inventory.setItem(4, sword);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}