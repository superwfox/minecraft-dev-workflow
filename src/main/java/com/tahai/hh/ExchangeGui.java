package com.tahai.hh;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ExchangeGui implements InventoryHolder, Listener {

    private Inventory inventory;

    public ExchangeGui() {
        inventory = Bukkit.createInventory(this, 9, "金币兑换点券");
        initializeItems();
    }

    private void initializeItems() {
        addItem(0, "兑换100金币", "消耗100金币获得1点券", Material.GOLD_NUGGET);
        addItem(1, "兑换1000金币", "消耗1000金币获得10点券", Material.GOLD_NUGGET);
        addItem(2, "兑换10000金币", "消耗10000金币获得100点券", Material.GOLD_INGOT);
        addItem(3, "兑换100000金币", "消耗100000金币获得1000点券", Material.GOLD_INGOT);
    }

    private void addItem(int slot, String name, String lore, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            loreList.add(lore);
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        inventory.setItem(slot, item);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}