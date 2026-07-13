package com.tahai.randomfishingloot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class FishingListener implements Listener {

    private final ConfigManager configManager;
    private final LootManager lootManager;

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
        if (new Random().nextDouble() >= chance) {
            return;
        }

        int minEnchants = configManager.getEnchantCountMin();
        int maxEnchants = configManager.getEnchantCountMax();
        List<String> equipmentPool = configManager.getEquipmentPool();

        ItemStack loot = lootManager.generateRandomLoot(equipmentPool, minEnchants, maxEnchants, new Random());

        Player player = event.getPlayer();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(loot);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover.values().iterator().next());
        }
    }
}