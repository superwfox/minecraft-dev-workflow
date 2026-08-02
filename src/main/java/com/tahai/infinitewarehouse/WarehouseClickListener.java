package com.tahai.infinitewarehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WarehouseClickListener implements Listener {

    private static final String AMOUNT_PREFIX = ChatColor.GRAY + "数量: ";

    private final DataManager dataManager;

    public WarehouseClickListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof WarehouseHolder)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        int rawSlot = event.getRawSlot();
        int size = inv.getSize();

        if (rawSlot < 0) {
            event.setCancelled(true);
            return;
        }

        if (rawSlot >= size) {
            if (!event.isShiftClick()) {
                event.setCancelled(false);
                return;
            }
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getAmount() > 0) {
                if (addToWarehouse(player, inv, clicked)) {
                    event.getView().getBottomInventory().setItem(event.getSlot(), null);
                }
            }
            dataManager.saveWarehouse(player);
            return;
        }

        event.setCancelled(true);
        ItemStack clicked = inv.getItem(rawSlot);
        ItemStack cursor = event.getCursor();

        if (event.isShiftClick()) {
            if (clicked != null) {
                takeOut(player, inv, rawSlot, 64);
            }
        } else if (event.isLeftClick()) {
            if (cursor != null && cursor.getAmount() > 0) {
                if (deposit(player, inv, rawSlot, cursor, cursor.getAmount())) {
                    event.setCursor(null);
                }
            } else if (clicked != null) {
                takeOut(player, inv, rawSlot, 1);
            }
        } else if (event.isRightClick()) {
            if (cursor != null && cursor.getAmount() > 0) {
                if (deposit(player, inv, rawSlot, cursor, 1)) {
                    cursor.setAmount(cursor.getAmount() - 1);
                    event.setCursor(cursor.getAmount() > 0 ? cursor : null);
                }
            } else if (clicked != null) {
                takeOut(player, inv, rawSlot, 1);
            }
        }

        dataManager.saveWarehouse(player);
    }

    private boolean deposit(Player player, Inventory inv, int slot, ItemStack incoming, int amount) {
        ItemStack[] contents = dataManager.getWarehouse(player);
        if (contents == null || slot >= contents.length) {
            return false;
        }
        ItemStack stored = contents[slot];
        ItemStack sample = incoming.clone();
        sample.setAmount(1);

        if (stored == null) {
            ItemStack copy = incoming.clone();
            copy.setAmount(amount);
            contents[slot] = copy;
            refreshSlot(inv, slot, copy);
            return true;
        }
        if (stored.isSimilar(sample)) {
            stored.setAmount(stored.getAmount() + amount);
            refreshSlot(inv, slot, stored);
            return true;
        }
        player.sendMessage(ChatColor.AQUA + "该槽位已被其他物品占用");
        return false;
    }

    private void takeOut(Player player, Inventory inv, int slot, int amount) {
        ItemStack[] contents = dataManager.getWarehouse(player);
        if (contents == null || slot >= contents.length) {
            return;
        }
        ItemStack stored = contents[slot];
        if (stored == null) {
            return;
        }

        int take = Math.min(amount, stored.getAmount());
        if (take <= 0) {
            return;
        }

        ItemStack give = stored.clone();
        give.setAmount(take);
        stored.setAmount(stored.getAmount() - take);
        if (stored.getAmount() <= 0) {
            contents[slot] = null;
        }

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(give);
        for (ItemStack item : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        if (contents[slot] == null) {
            inv.setItem(slot, null);
        } else {
            refreshSlot(inv, slot, contents[slot]);
        }
    }

    private boolean addToWarehouse(Player player, Inventory inv, ItemStack item) {
        ItemStack[] contents = dataManager.getWarehouse(player);
        if (contents == null) {
            return false;
        }

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].isSimilar(item)) {
                contents[i].setAmount(contents[i].getAmount() + item.getAmount());
                refreshSlot(inv, i, contents[i]);
                return true;
            }
        }

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                ItemStack copy = item.clone();
                contents[i] = copy;
                refreshSlot(inv, i, copy);
                return true;
            }
        }

        player.sendMessage(ChatColor.AQUA + "仓库已满");
        return false;
    }

    private void refreshSlot(Inventory inv, int slot, ItemStack stored) {
        ItemStack display = stored.clone();
        int amount = display.getAmount();
        display.setAmount(1);

        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            inv.setItem(slot, display);
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<String>();
        }

        boolean replaced = false;
        for (int i = 0; i < lore.size(); i++) {
            String line = ChatColor.stripColor(lore.get(i));
            if (line != null && line.matches("数量: \\d+")) {
                lore.set(i, AMOUNT_PREFIX + amount);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lore.add(AMOUNT_PREFIX + amount);
        }

        meta.setLore(lore);
        display.setItemMeta(meta);
        inv.setItem(slot, display);
    }
}