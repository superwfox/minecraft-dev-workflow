package com.tahai.supervault;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VaultGuiListener implements Listener {

    private final PlayerVaultManager manager;

    public VaultGuiListener(PlayerVaultManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof VaultGuiHolder)) {
            return;
        }
        event.setCancelled(true);

        Inventory top = event.getView().getTopInventory();
        Inventory bottom = event.getView().getBottomInventory();
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        ClickType click = event.getClick();

        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
            if (clicked == top) {
                moveToPlayer(top, bottom, slot);
            } else {
                moveToVault(bottom, top, slot);
            }
            syncAndRefresh((VaultGuiHolder) top.getHolder(), player);
            return;
        }

        if (click == ClickType.LEFT) {
            leftClick(clicked, top, slot, event.getCursor(), event);
            syncAndRefresh((VaultGuiHolder) top.getHolder(), player);
        } else if (click == ClickType.RIGHT) {
            rightClick(clicked, top, slot, event.getCursor(), event);
            syncAndRefresh((VaultGuiHolder) top.getHolder(), player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof VaultGuiHolder) {
            manager.saveVault(((Player) event.getPlayer()).getUniqueId());
        }
    }

    private void leftClick(Inventory clicked, Inventory top, int slot, ItemStack cursor, InventoryClickEvent event) {
        ItemStack current = clicked.getItem(slot);
        if (current == null) {
            if (cursor == null) {
                return;
            }
            if (clicked == top) {
                clicked.setItem(slot, cursor.clone());
                event.setCursor(null);
            } else if (cursor.getAmount() <= cursor.getMaxStackSize()) {
                clicked.setItem(slot, cursor.clone());
                event.setCursor(null);
            } else {
                ItemStack place = cursor.clone();
                place.setAmount(cursor.getMaxStackSize());
                clicked.setItem(slot, place);
                cursor.setAmount(cursor.getAmount() - cursor.getMaxStackSize());
                event.setCursor(cursor);
            }
            return;
        }

        if (cursor == null) {
            clicked.setItem(slot, null);
            event.setCursor(current.clone());
            return;
        }

        if (current.isSimilar(cursor)) {
            if (clicked == top) {
                current.setAmount(current.getAmount() + cursor.getAmount());
                clicked.setItem(slot, current);
                event.setCursor(null);
            } else {
                int space = current.getMaxStackSize() - current.getAmount();
                if (space <= 0) {
                    return;
                }
                int add = Math.min(space, cursor.getAmount());
                current.setAmount(current.getAmount() + add);
                cursor.setAmount(cursor.getAmount() - add);
                clicked.setItem(slot, current);
                event.setCursor(cursor.getAmount() > 0 ? cursor : null);
            }
            return;
        }

        if (clicked == top || cursor.getAmount() <= cursor.getMaxStackSize()) {
            clicked.setItem(slot, cursor.clone());
            event.setCursor(current.clone());
        }
    }

    private void rightClick(Inventory clicked, Inventory top, int slot, ItemStack cursor, InventoryClickEvent event) {
        ItemStack current = clicked.getItem(slot);

        if (current == null) {
            if (cursor == null || cursor.getAmount() <= 0) {
                return;
            }
            ItemStack one = cursor.clone();
            one.setAmount(1);
            clicked.setItem(slot, one);
            if (cursor.getAmount() <= 1) {
                event.setCursor(null);
            } else {
                cursor.setAmount(cursor.getAmount() - 1);
                event.setCursor(cursor);
            }
            return;
        }

        if (cursor == null) {
            int take = (current.getAmount() + 1) / 2;
            ItemStack split = current.clone();
            split.setAmount(take);
            if (take >= current.getAmount()) {
                clicked.setItem(slot, null);
            } else {
                current.setAmount(current.getAmount() - take);
                clicked.setItem(slot, current);
            }
            event.setCursor(split);
            return;
        }

        if (current.isSimilar(cursor)) {
            if (clicked == top || current.getAmount() < current.getMaxStackSize()) {
                current.setAmount(current.getAmount() + 1);
                clicked.setItem(slot, current);
                if (cursor.getAmount() <= 1) {
                    event.setCursor(null);
                } else {
                    cursor.setAmount(cursor.getAmount() - 1);
                    event.setCursor(cursor);
                }
            }
        }
    }

    private void moveToPlayer(Inventory top, Inventory bottom, int slot) {
        ItemStack item = top.getItem(slot);
        if (item == null) {
            return;
        }
        ItemStack moving = item.clone();
        for (int i = 0; i < bottom.getSize(); i++) {
            ItemStack target = bottom.getItem(i);
            if (target == null) {
                ItemStack place = moving.clone();
                if (place.getAmount() > place.getMaxStackSize()) {
                    place.setAmount(place.getMaxStackSize());
                    bottom.setItem(i, place);
                    moving.setAmount(moving.getAmount() - place.getAmount());
                } else {
                    bottom.setItem(i, place);
                    top.setItem(slot, null);
                    return;
                }
            } else if (target.isSimilar(moving)) {
                int space = target.getMaxStackSize() - target.getAmount();
                if (space > 0) {
                    int add = Math.min(space, moving.getAmount());
                    target.setAmount(target.getAmount() + add);
                    bottom.setItem(i, target);
                    moving.setAmount(moving.getAmount() - add);
                    if (moving.getAmount() <= 0) {
                        top.setItem(slot, null);
                        return;
                    }
                }
            }
        }
        top.setItem(slot, moving);
    }

    private void moveToVault(Inventory bottom, Inventory top, int slot) {
        ItemStack item = bottom.getItem(slot);
        if (item == null) {
            return;
        }
        ItemStack moving = item.clone();
        for (int i = 0; i < top.getSize(); i++) {
            ItemStack target = top.getItem(i);
            if (target == null) {
                top.setItem(i, moving);
                bottom.setItem(slot, null);
                return;
            }
            if (target.isSimilar(moving)) {
                target.setAmount(target.getAmount() + moving.getAmount());
                top.setItem(i, target);
                bottom.setItem(slot, null);
                return;
            }
        }
    }

    private void syncAndRefresh(VaultGuiHolder holder, Player player) {
        PlayerVault vault = manager.getVault(player.getUniqueId());
        if (vault != null) {
            vault.setContents(holder.getInventory().getContents());
        }
        holder.refresh();
    }
}