package com.tahai.ngpumpkinpie;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PieUseListener implements Listener {

    private static final long COOLDOWN_MS = 30000; // 30秒冷却
    private static final double RADIUS = 3.0;
    private static final int DURATION_TICKS = 5 * 60 * 20; // 5分钟
    private static final int AMPLIFIER = 1; // 饱和II

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.PUMPKIN_PIE) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();
        long cdEnd = DataManager.getCooldown(uuid);
        if (cdEnd > now) {
            player.sendMessage(ChatColor.RED + "冷却中，请稍后再试");
            return;
        }

        event.setCancelled(true);
        item.setAmount(item.getAmount() - 1);

        PotionEffect effect = new PotionEffect(PotionEffectType.SATURATION, DURATION_TICKS, AMPLIFIER);
        player.addPotionEffect(effect);
        player.sendMessage(ChatColor.GREEN + "你使用了南瓜派，获得了饱和效果！");

        for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (entity instanceof Player) {
                Player p = (Player) entity;
                p.addPotionEffect(effect);
                p.sendMessage(ChatColor.GREEN + "你获得了饱和效果！");
            }
        }

        DataManager.setCooldown(uuid, now + COOLDOWN_MS);
    }

    private static class DataManager {
        private static final Map<UUID, Long> cooldowns = new HashMap<>();

        public static long getCooldown(UUID uuid) {
            return cooldowns.getOrDefault(uuid, 0L);
        }

        public static void setCooldown(UUID uuid, long endTime) {
            cooldowns.put(uuid, endTime);
        }
    }
}