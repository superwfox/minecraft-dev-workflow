package com.tahai.sellvaultgui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class SellGui implements InventoryHolder, Listener {

    private final Inventory inventory;

    public SellGui() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SellVaultGui");
        String title = "Sell Vault";
        if (plugin != null) {
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            YamlConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
            title = messages.getString("gui.title", title);
        }
        this.inventory = Bukkit.createInventory(this, 27, ChatColor.translateAlternateColorCodes('&', title));
        this.inventory.setItem(22, new ItemStack(Material.GOLD_INGOT));
    }

    public static SellGui create() {
        return new SellGui();
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}