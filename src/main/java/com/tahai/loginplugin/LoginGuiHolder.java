package com.tahai.loginplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryType;

public class LoginGuiHolder implements Listener, InventoryHolder {

    private Inventory inventory;

    public static void openLogin(Player player) {
        LoginGuiHolder holder = new LoginGuiHolder();
        holder.inventory = Bukkit.createInventory(holder, InventoryType.ANVIL, "登录");
        player.openInventory(holder.inventory);
    }

    public static void openRegister(Player player) {
        LoginGuiHolder holder = new LoginGuiHolder();
        holder.inventory = Bukkit.createInventory(holder, InventoryType.ANVIL, "注册");
        player.openInventory(holder.inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}