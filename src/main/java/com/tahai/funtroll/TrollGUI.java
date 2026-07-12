package com.tahai.funtroll;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class TrollGUI implements Listener, InventoryHolder {

    private static final Map<Inventory, TrollData> activeInventories = new HashMap<>();

    private static class TrollData {
        final Player opener;
        final Player target;
        TrollData(Player opener, Player target) {
            this.opener = opener;
            this.target = target;
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public void openMainMenu(Player opener) {
        Inventory inv = Bukkit.createInventory(this, 9, ChatColor.GOLD + "FunTroll Main Menu");
        ItemStack playerListItem = new ItemStack(Material.BOOK);
        ItemMeta meta = playerListItem.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Player List");
        playerListItem.setItemMeta(meta);
        inv.setItem(0, playerListItem);
        activeInventories.put(inv, new TrollData(opener, null));
        opener.openInventory(inv);
    }

    public void openPlayerList(Player opener) {
        int size = (int) Math.ceil((double) Bukkit.getOnlinePlayers().size() / 9) * 9;
        if (size < 9) size = 9;
        Inventory inv = Bukkit.createInventory(this, size, ChatColor.GOLD + "Online Players");
        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (slot >= size) break;
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + p.getName());
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }
        activeInventories.put(inv, new TrollData(opener, null));
        opener.openInventory(inv);
    }

    public void openPlayerTrollMenu(Player opener, Player target) {
        Inventory inv = Bukkit.createInventory(this, 9, ChatColor.GOLD + "Troll " + target.getName());
        ItemStack freezeItem = new ItemStack(Material.PACKED_ICE);
        ItemMeta meta = freezeItem.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Freeze");
        freezeItem.setItemMeta(meta);
        inv.setItem(0, freezeItem);

        ItemStack anvilItem = new ItemStack(Material.ANVIL);
        meta = anvilItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Anvil");
        anvilItem.setItemMeta(meta);
        inv.setItem(1, anvilItem);

        ItemStack lightningItem = new ItemStack(Material.LIGHTNING_ROD);
        meta = lightningItem.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Lightning");
        lightningItem.setItemMeta(meta);
        inv.setItem(2, lightningItem);

        activeInventories.put(inv, new TrollData(opener, target));
        opener.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (inv.getHolder() instanceof TrollGUI) {
            activeInventories.remove(inv);
        }
    }
}