package com.tahai.randomfishingloot;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FishingListener implements Listener {

    private final ConfigManager configManager;
    private final LootManager lootManager;
    private final Random random = new Random();

    public FishingListener(ConfigManager configManager, LootManager lootManager) {
        this.configManager = configManager;
        this.lootManager = lootManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        if (!configManager.isEnabled()) {
            return;
        }

        double chance = configManager.getChance();
        if (random.nextDouble() >= chance) {
            return;
        }

        ItemStack loot = lootManager.generateRandomLoot();
        if (loot == null || loot.getType() == Material.AIR) {
            return;
        }

        Player player = event.getPlayer();
        boolean added = addToInventoryOrDrop(player, loot);
        // 不改变原有掉落物
    }

    private boolean addToInventoryOrDrop(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item).forEach((index, leftover) -> {
                player.getWorld().dropItem(player.getLocation(), leftover);
            });
            return true;
        } else {
            player.getWorld().dropItem(player.getLocation(), item);
            return false;
        }
    }
}