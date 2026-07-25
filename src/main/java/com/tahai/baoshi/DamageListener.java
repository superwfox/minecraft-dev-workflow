package com.tahai.baoshi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;
import java.util.UUID;

public class DamageListener implements Listener {

    private DataManager dataManager;
    private final Random random = new Random();

    private DataManager getDataManager() {
        if (dataManager == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Baoshi");
            dataManager = new DataManager(plugin);
        }
        return dataManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        double damage = event.getDamage();

        DataManager dm = getDataManager();

        // Attacker effects
        if (damager instanceof Player) {
            Player attacker = (Player) damager;
            ItemStack[] armor = attacker.getInventory().getArmorContents();
            int attackBonus = 0;
            int critChance = 0;

            for (ItemStack item : armor) {
                if (item == null) continue;
                UUID uuid = NbtUtil.getUUID(item);
                if (uuid == null) continue;

                String type = dm.getGemType(uuid);
                int level = dm.getGemLevel(uuid);

                if ("青鳞石".equals(type)) {
                    attackBonus += level;
                } else if ("落凤石".equals(type)) {
                    critChance += level * 5;
                }
            }

            damage += attackBonus;
            if (critChance > 0 && random.nextInt(100) < critChance) {
                damage *= 2;
            }
        }

        // Defender effects
        if (damagee instanceof Player) {
            Player victim = (Player) damagee;
            ItemStack[] armor = victim.getInventory().getArmorContents();
            int defense = 0;

            for (ItemStack item : armor) {
                if (item == null) continue;
                UUID uuid = NbtUtil.getUUID(item);
                if (uuid == null) continue;

                String type = dm.getGemType(uuid);
                int level = dm.getGemLevel(uuid);

                if ("灵犀石".equals(type)) {
                    defense += level;
                }
            }

            double reduction = (double) defense / (defense + 100);
            damage *= (1 - reduction);
        }

        event.setDamage(damage);
    }
}