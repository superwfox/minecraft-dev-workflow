package com.tahai.buildershield;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ShieldListener implements Listener {

    private final Plugin plugin;
    private final ConfigManager config;
    private final DataManager data;
    private final NamespacedKey shieldKey;

    public ShieldListener() {
        this.plugin = Bukkit.getPluginManager().getPlugin("BuilderShield");
        this.config = new ConfigManager();
        this.data = new DataManager();
        String key = config.getShieldItem();
        this.shieldKey = new NamespacedKey(plugin, key == null || key.isEmpty() ? "shield" : key);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !isShieldItem(item)) {
            return;
        }
        if (data.isShieldEnabled(player.getUniqueId())) {
            data.closeShield(player.getUniqueId());
            player.sendMessage(color(config.getMessageRemoved()));
        } else {
            consumeItem(player, event.getHand());
            data.openShield(player.getUniqueId());
            player.sendMessage(color(config.getMessagePlaced()));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager) {
            if (data.isShieldEnabled(damager.getUniqueId())) {
                event.setCancelled(true);
                String msg = config.getString("messages.attack-blocked", ChatColor.AQUA + "You are protected by your builder shield.");
                damager.sendMessage(color(msg));
            }
        }
    }

    private boolean isShieldItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(shieldKey, PersistentDataType.STRING);
    }

    private void consumeItem(Player player, EquipmentSlot hand) {
        EquipmentSlot slot = hand == null ? EquipmentSlot.HAND : hand;
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null) {
            return;
        }
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItem(slot, null);
        }
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}