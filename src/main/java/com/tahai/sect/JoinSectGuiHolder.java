package com.tahai.sect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class JoinSectGuiHolder implements Listener, InventoryHolder {

    private final Plugin plugin;
    private final List<String> guildNames;
    private final int page;
    private final int totalPages;
    private final Inventory inventory;

    public JoinSectGuiHolder(int page) {
        this.plugin = Bukkit.getPluginManager().getPlugin("Sect");
        this.guildNames = loadGuildNames();
        this.totalPages = Math.max(1, (guildNames.size() + 44) / 45);
        this.page = Math.max(1, Math.min(page, totalPages));
        this.inventory = Bukkit.createInventory(this, 54, "加入宗门 - 第 " + this.page + " / " + totalPages + " 页");
        populate();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public int getCurrentPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    private List<String> loadGuildNames() {
        List<String> names = new ArrayList<>();
        if (plugin == null) {
            return names;
        }
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("guilds");
        if (section != null) {
            names.addAll(section.getKeys(false));
        }
        return names;
    }

    private void populate() {
        int start = (page - 1) * 45;
        int end = Math.min(start + 45, guildNames.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            inventory.setItem(slot++, createGuildItem(guildNames.get(i)));
        }
        if (page > 1) {
            inventory.setItem(45, createNavItem(Material.ARROW, ChatColor.YELLOW + "上一页"));
        }
        if (page < totalPages) {
            inventory.setItem(53, createNavItem(Material.ARROW, ChatColor.YELLOW + "下一页"));
        }
        inventory.setItem(49, createNavItem(Material.PAPER, ChatColor.GRAY + "第 " + page + " / " + totalPages + " 页"));
    }

    private ItemStack createGuildItem(String guildName) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + guildName);

        List<String> lore = new ArrayList<>();
        String leader = plugin.getConfig().getString("guilds." + guildName + ".leader", "");
        int level = plugin.getConfig().getInt("guilds." + guildName + ".level", 1);
        int kills = plugin.getConfig().getInt("guilds." + guildName + ".kills", 0);
        ConfigurationSection members = plugin.getConfig().getConfigurationSection("guilds." + guildName + ".members");
        int memberCount = members == null ? 0 : members.getKeys(false).size();

        lore.add(ChatColor.GRAY + "宗主: " + leader);
        lore.add(ChatColor.GRAY + "等级: " + level);
        lore.add(ChatColor.GRAY + "击杀: " + kills);
        lore.add(ChatColor.GRAY + "成员: " + memberCount);

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}