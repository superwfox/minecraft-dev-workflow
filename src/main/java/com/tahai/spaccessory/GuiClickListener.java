package com.tahai.spaccessory;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class GuiClickListener implements Listener {
    private final Plugin plugin;
    private final PermissionManager permissionManager;

    public GuiClickListener(Plugin plugin, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.permissionManager = permissionManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof GuiHolder)) return;

        GuiHolder holder = (GuiHolder) inv.getHolder();
        int rawSlot = event.getRawSlot();
        if (rawSlot >= inv.getSize()) return; // 玩家背包槽位不干涉

        // 如果槽位不可用，禁止任何操作
        if (!holder.isSlotEnabled(rawSlot)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // 放入
        if (cursor != null && cursor.getType() != Material.AIR &&
            (current == null || current.getType() == Material.AIR)) {
            if (isAccessory(cursor)) {
                event.setCurrentItem(cursor.clone());
                event.setCursor(null);
                saveAccessories(player, inv, holder);
                addAccessoryPermission(player, cursor);
            }
            return;
        }

        // 拿出
        if ((cursor == null || cursor.getType() == Material.AIR) &&
            current != null && current.getType() != Material.AIR) {
            if (isAccessory(current)) {
                event.setCursor(current.clone());
                event.setCurrentItem(null);
                saveAccessories(player, inv, holder);
                removeAccessoryPermission(player, current);
            }
        }
    }

    private void saveAccessories(Player player, Inventory inv, GuiHolder holder) {
        List<ItemStack> items = holder.getAccessoryItems();
        PlayerDataUtil.saveItemsToPDC(player, items);
    }

    private boolean isAccessory(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        List<String> lore = meta.getLore();
        if (lore == null || !lore.contains("饰品")) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(new NamespacedKey("spaccessory", "Sp"), PersistentDataType.STRING);
    }

    private void addAccessoryPermission(Player player, ItemStack item) {
        String spValue = getSpValue(item);
        if (spValue != null) {
            permissionManager.addPermission(player, "sp." + spValue);
        }
    }

    private void removeAccessoryPermission(Player player, ItemStack item) {
        String spValue = getSpValue(item);
        if (spValue != null) {
            permissionManager.removePermission(player, "sp." + spValue);
        }
    }

    private String getSpValue(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(new NamespacedKey("spaccessory", "Sp"), PersistentDataType.STRING);
    }
}