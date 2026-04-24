package com.example.landplugin;

import com.example.landplugin.LandData;
import com.example.landplugin.LandManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryGUI {

    private final Plugin plugin;
    private final LandManager landManager;

    public InventoryGUI() {
        this.plugin = Bukkit.getPluginManager().getPlugin("LandPlugin");
        if (this.plugin == null) {
            throw new IllegalStateException("LandPlugin not found!");
        }
        // 通过主类获取LandManager，假设主类有一个getLandManager()方法
        try {
            Class<?> mainClass = Class.forName("com.example.landplugin.LandPlugin");
            java.lang.reflect.Method getLandManagerMethod = mainClass.getMethod("getLandManager");
            this.landManager = (LandManager) getLandManagerMethod.invoke(this.plugin);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get LandManager from LandPlugin", e);
        }
    }

    public void openLandListGUI(Player player) {
        Map<String, LandData> allLands = landManager.getAllLands();
        List<LandData> playerLands = new ArrayList<>();
        for (LandData land : allLands.values()) {
            if (land.isOwner(player.getUniqueId())) {
                playerLands.add(land);
            }
        }

        Inventory inv = Bukkit.createInventory(null, 54, "我的领地列表");
        for (int i = 0; i < Math.min(playerLands.size(), 54); i++) {
            LandData land = playerLands.get(i);
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a领地: " + land.getWorldName() + " (" + land.getChunkX() + ", " + land.getChunkZ() + ")");
            List<String> lore = new ArrayList<>();
            lore.add("§7拥有者: " + Bukkit.getOfflinePlayer(land.getOwnerUUID()).getName());
            lore.add("§7信任成员: " + land.getTrustedMembers().size() + " 人");
            lore.add("§e左键点击管理信任成员");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
        player.openInventory(inv);
    }

    public void openTrustedMembersGUI(Player player, LandData land) {
        Inventory inv = Bukkit.createInventory(null, 54, "管理信任成员 - " + land.getChunkKey());
        List<UUID> trusted = land.getTrustedMembers();
        for (int i = 0; i < Math.min(trusted.size(), 45); i++) {
            UUID uuid = trusted.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            skullMeta.setDisplayName("§b" + Bukkit.getOfflinePlayer(uuid).getName());
            List<String> lore = new ArrayList<>();
            lore.add("§c右键点击移除信任");
            skullMeta.setLore(lore);
            head.setItemMeta(skullMeta);
            inv.setItem(i, head);
        }

        ItemStack addButton = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addButton.getItemMeta();
        addMeta.setDisplayName("§a添加信任成员");
        List<String> addLore = new ArrayList<>();
        addLore.add("§7点击后输入玩家名");
        addMeta.setLore(addLore);
        addButton.setItemMeta(addMeta);
        inv.setItem(49, addButton);

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c返回领地列表");
        backButton.setItemMeta(backMeta);
        inv.setItem(53, backButton);

        player.openInventory(inv);
    }

    public LandManager getLandManager() {
        return landManager;
    }
}