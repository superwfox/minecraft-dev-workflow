package me.iw46dev.itemcleanertitle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class MainGUIHolder implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Plugin plugin;

    public MainGUIHolder(Plugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 9, ChatColor.GOLD + "主菜单");
        initializeItems();
    }

    private void initializeItems() {
        inventory.setItem(0, createItem(Material.CLOCK, ChatColor.GREEN + "设置清理时间",
                ChatColor.GRAY + "点击设置清理物品的时间间隔"));
        inventory.setItem(1, createItem(Material.BOOK, ChatColor.GREEN + "设置清理文本",
                ChatColor.GRAY + "点击设置清理消息内容"));
        inventory.setItem(2, createItem(Material.PAPER, ChatColor.GREEN + "添加头衔",
                ChatColor.GRAY + "点击添加一个头衔"));
        inventory.setItem(3, createItem(Material.REDSTONE, ChatColor.RED + "删除头衔",
                ChatColor.GRAY + "点击删除一个头衔"));
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}