package com.tahai.trinketchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GUIHolder implements InventoryHolder {

    private static final NamespacedKey ITEMS_KEY;
    private static final NamespacedKey SIZE_KEY;

    static {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("TrinketChest");
        ITEMS_KEY = new NamespacedKey(plugin, "items");
        SIZE_KEY = new NamespacedKey(plugin, "size");
    }

    private final Inventory inventory;
    private final Player owner;
    private final int size;

    public GUIHolder(Player player, int size) {
        this.owner = player;
        this.size = size;
        int invSize = Math.max(9, Math.min(54, ((size + 8) / 9) * 9));
        String title = ChatColor.GOLD + "Trinket Chest";
        this.inventory = Bukkit.createInventory(this, invSize, title);
        loadItems();
        fillUnusedSlots();
    }

    private void loadItems() {
        List<ItemStack> items = loadItemsFromPDC(owner);
        for (int i = 0; i < size && i < items.size(); i++) {
            inventory.setItem(i, items.get(i));
        }
    }

    private void fillUnusedSlots() {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = size; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    // Serialization utilities

    public static String serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
            boos.writeObject(item);
            boos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack", e);
        }
    }

    public static ItemStack deserializeItemStack(String data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);
            ItemStack item = (ItemStack) bois.readObject();
            bois.close();
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack", e);
        }
    }

    // Persistent Data Container helpers

    public static void saveItemsToPDC(Player player, List<ItemStack> items, int size) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
            boos.writeObject(new ArrayList<>(items)); // ensure serializable list
            boos.close();
            String encoded = Base64.getEncoder().encodeToString(baos.toByteArray());
            player.getPersistentDataContainer().set(ITEMS_KEY, PersistentDataType.STRING, encoded);
            player.getPersistentDataContainer().set(SIZE_KEY, PersistentDataType.INTEGER, size);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save items to PDC", e);
        }
    }

    public static List<ItemStack> loadItemsFromPDC(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(ITEMS_KEY, PersistentDataType.STRING)) {
            return new ArrayList<>();
        }
        try {
            String encoded = pdc.get(ITEMS_KEY, PersistentDataType.STRING);
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);
            List<ItemStack> items = (List<ItemStack>) bois.readObject();
            bois.close();
            return items;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load items from PDC", e);
        }
    }

    public static int loadSizeFromPDC(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.getOrDefault(SIZE_KEY, PersistentDataType.INTEGER, 0);
    }
}