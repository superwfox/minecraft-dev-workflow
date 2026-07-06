package com.tahai.trinketchest;

import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) {
            return;
        }
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getInventory().getSize()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TrinketChest");
        if (plugin == null) return;

        // 放入逻辑
        if (cursor != null && (current == null || current.getType().isAir())) {
            if (!isTrinketItem(cursor, plugin)) return;

            String spValue = getSPValue(cursor, plugin);
            if (spValue == null || spValue.isEmpty()) return;

            // 赋予权限
            player.addAttachment(plugin, "sp." + spValue, true);

            // 物品放入箱子
            inv.setItem(rawSlot, cursor.clone());
            event.setCursor(null);

            // 更新PDC
            saveInventoryToPDC(player, inv, plugin);
        }
        // 拿出逻辑
        else if (cursor == null && current != null && !current.getType().isAir()) {
            if (!isTrinketItem(current, plugin)) return;

            String spValue = getSPValue(current, plugin);
            if (spValue == null || spValue.isEmpty()) return;

            // 检查箱内是否还存在相同SP值的物品（排除当前槽）
            boolean otherExists = false;
            for (int i = 0; i < inv.getSize(); i++) {
                if (i == rawSlot) continue;
                ItemStack item = inv.getItem(i);
                if (item != null && isTrinketItem(item, plugin)) {
                    String otherSP = getSPValue(item, plugin);
                    if (spValue.equals(otherSP)) {
                        otherExists = true;
                        break;
                    }
                }
            }
            if (!otherExists) {
                // 删除权限
                player.addAttachment(plugin, "sp." + spValue, false);
            }

            // 将物品给玩家
            event.setCursor(current.clone());
            inv.setItem(rawSlot, null);

            // 更新PDC
            saveInventoryToPDC(player, inv, plugin);
        }
    }

    private boolean isTrinketItem(ItemStack item, Plugin plugin) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        // 检查Lore
        if (meta.hasLore()) {
            boolean found = false;
            for (String line : meta.getLore()) {
                if (line.contains("饰品")) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        } else {
            return false;
        }
        // 检查NBT:SP
        NamespacedKey key = new NamespacedKey(plugin, "SP");
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(key, PersistentDataType.STRING);
    }

    private String getSPValue(ItemStack item, Plugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "SP");
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(key, PersistentDataType.STRING);
    }

    private void saveInventoryToPDC(Player player, Inventory inv, Plugin plugin) {
        GUIHolder holder = (GUIHolder) inv.getHolder();
        List<ItemStack> items = new ArrayList<>(Arrays.asList(inv.getContents()));
        holder.saveItemsToPDC(player, items, inv.getSize());
    }
}