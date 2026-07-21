package com.tahai.lottery;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class KeyUseListener implements Listener {

    private final NamespacedKey keyKey;

    public KeyUseListener() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Lottery");
        if (plugin == null) {
            throw new IllegalStateException("Lottery plugin not found");
        }
        this.keyKey = new NamespacedKey(plugin, "lottery-key");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        if (!meta.getPersistentDataContainer().has(keyKey)) {
            return;
        }

        // 消耗一个钥匙
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // 取消事件以防止默认行为
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);

        // 打开箱子选择GUI
        GUIHolder holder = new GUIHolder(GUIHolder.GUIType.BOX_SELECT, null, null, null);
        player.openInventory(holder.createBoxSelectGUI());
    }
}