package com.tahai.supervault;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VaultGuiHolder implements InventoryHolder {

    private final PlayerVaultManager manager;
    private final Player owner;
    private final Inventory inventory;

    public VaultGuiHolder(PlayerVaultManager manager, Player owner) {
        this.manager = manager;
        this.owner = owner;
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.YELLOW + "超级仓库");
        refresh();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();
        PlayerVault vault = manager.getVault(owner.getUniqueId());
        ItemStack[] contents = vault.getContents();
        for (int i = 0; i < contents.length && i < inventory.getSize(); i++) {
            if (contents[i] != null) {
                inventory.setItem(i, toDisplayItem(contents[i]));
            }
        }
    }

    private ItemStack toDisplayItem(ItemStack item) {
        ItemStack display = item.clone();
        ItemMeta meta = display.getItemMeta();
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "数量: " + display.getAmount()));
        display.setItemMeta(meta);
        return display;
    }
}