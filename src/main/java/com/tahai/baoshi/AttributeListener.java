package com.tahai.baoshi;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class AttributeListener implements Listener {

    private static final double MAX_HEALTH_PER_LEVEL = 2.0;
    private static final double MELEE_DAMAGE_PER_LEVEL = 2.0;
    private static final double CRIT_CHANCE_PER_LEVEL = 0.1;
    private static final double DEFENSE_PER_LEVEL = 5.0;
    private final Random random = new Random();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateMaxHealth(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        if (damager instanceof Player) {
            Player p = (Player) damager;
            updateMaxHealth(p);

            ItemStack mainHand = p.getInventory().getItemInMainHand();
            ItemStack offHand = p.getInventory().getItemInOffHand();
            ItemStack[] hands = { mainHand, offHand };

            for (ItemStack hand : hands) {
                if (hand != null && GemHelper.isGem(hand)) {
                    GemHelper.GemType type = GemHelper.getGemType(hand);
                    int level = GemHelper.getGemLevel(hand);
                    if (type == GemHelper.GemType.EMERALD) {
                        event.setDamage(event.getDamage() + level * MELEE_DAMAGE_PER_LEVEL);
                    }
                    if (type == GemHelper.GemType.DIAMOND) {
                        double critChance = level * CRIT_CHANCE_PER_LEVEL;
                        if (random.nextDouble() < critChance) {
                            event.setDamage(event.getDamage() * 2);
                        }
                    }
                }
            }
        }

        if (victim instanceof Player) {
            Player p = (Player) victim;
            updateMaxHealth(p);
            double defense = 0;
            for (ItemStack armor : p.getInventory().getArmorContents()) {
                if (armor != null && GemHelper.isGem(armor)) {
                    GemHelper.GemType type = GemHelper.getGemType(armor);
                    int level = GemHelper.getGemLevel(armor);
                    if (type == GemHelper.GemType.SAPPHIRE) {
                        defense += level * DEFENSE_PER_LEVEL;
                    }
                }
            }
            if (defense > 0) {
                double reduced = event.getDamage() * (1 - defense / (defense + 100));
                event.setDamage(Math.max(0, reduced));
            }
        }
    }

    private void updateMaxHealth(Player player) {
        double bonusHealth = 0;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand != null && GemHelper.isGem(hand)) {
            GemHelper.GemType type = GemHelper.getGemType(hand);
            int level = GemHelper.getGemLevel(hand);
            if (type == GemHelper.GemType.RUBY) {
                bonusHealth += level * MAX_HEALTH_PER_LEVEL;
            }
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && GemHelper.isGem(offhand)) {
            GemHelper.GemType type = GemHelper.getGemType(offhand);
            int level = GemHelper.getGemLevel(offhand);
            if (type == GemHelper.GemType.RUBY) {
                bonusHealth += level * MAX_HEALTH_PER_LEVEL;
            }
        }
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && GemHelper.isGem(armor)) {
                GemHelper.GemType type = GemHelper.getGemType(armor);
                int level = GemHelper.getGemLevel(armor);
                if (type == GemHelper.GemType.RUBY) {
                    bonusHealth += level * MAX_HEALTH_PER_LEVEL;
                }
            }
        }
        double newMax = 20.0 + bonusHealth;
        player.setMaxHealth(newMax);
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }
}