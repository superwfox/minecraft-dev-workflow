package com.tahai.pvpduel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class DuelGUI implements Listener, InventoryHolder {

    private final Inventory inventory;

    public DuelGUI(Player inviter, String title) {
        List<Player> targets = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(inviter)) {
                targets.add(online);
            }
        }
        int size = targets.isEmpty() ? 9 : Math.min(54, ((targets.size() - 1) / 9 + 1) * 9);
        this.inventory = Bukkit.createInventory(this, size, title);
        for (int i = 0; i < Math.min(targets.size(), size); i++) {
            inventory.setItem(i, createHead(targets.get(i)));
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private ItemStack createHead(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName(ChatColor.YELLOW + target.getName());
        head.setItemMeta(meta);
        return head;
    }
}