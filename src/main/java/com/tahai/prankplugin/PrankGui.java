package com.tahai.prankplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PrankGui implements InventoryHolder {

    private final Inventory inventory;

    private PrankGui() {
        int playerCount = Bukkit.getOnlinePlayers().size();
        int size = ((playerCount / 9) + 1) * 9;
        if (size < 9) size = 9;
        if (size > 54) size = 54;
        this.inventory = Bukkit.createInventory(this, size, ChatColor.DARK_PURPLE + "选择恶搞玩家");
        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            inventory.setItem(slot, getPlayerHead(p));
            slot++;
        }
    }

    private ItemStack getPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.GOLD + player.getName());
        head.setItemMeta(meta);
        return head;
    }

    public static Inventory createGui() {
        return new PrankGui().getInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}