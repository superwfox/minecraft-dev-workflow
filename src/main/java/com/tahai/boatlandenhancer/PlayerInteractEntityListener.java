package com.tahai.boatlandenhancer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class PlayerInteractEntityListener implements Listener {

    private final Plugin plugin;

    public PlayerInteractEntityListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Boat boat)) return;

        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack item = player.getInventory().getItem(hand);
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof EnchantmentStorageMeta enchantMeta)) return;

        Map<Enchantment, Integer> storedEnchants = enchantMeta.getStoredEnchants();
        if (storedEnchants.isEmpty()) return;

        Map.Entry<Enchantment, Integer> entry = storedEnchants.entrySet().iterator().next();
        Enchantment enchantment = entry.getKey();
        int level = entry.getValue();

        PersistentDataContainer pdc = boat.getPersistentDataContainer();
        NamespacedKey enchantKey = new NamespacedKey(plugin, "enchantment");
        NamespacedKey levelKey = new NamespacedKey(plugin, "enchantment_level");

        pdc.set(enchantKey, PersistentDataType.STRING, enchantment.getKey().toString());
        pdc.set(levelKey, PersistentDataType.INTEGER, level);
    }
}