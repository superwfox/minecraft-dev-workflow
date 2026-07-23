package com.tahai.xnchallenge;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

public class ChallengeListener implements Listener {

    private final ConfigManager configManager;
    private final ChallengeManager challengeManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> activeChallenges = new HashSet<>();
    private static final String CHALLENGE_ITEM_NAME = "§e挑战令牌";

    public ChallengeListener(ConfigManager configManager, ChallengeManager challengeManager) {
        this.configManager = configManager;
        this.challengeManager = challengeManager;
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
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        if (!meta.getDisplayName().equals(CHALLENGE_ITEM_NAME)) {
            return;
        }

        // 检查世界
        if (!configManager.getAllowedWorlds().contains(player.getWorld().getName())) {
            player.sendMessage(org.bukkit.ChatColor.GRAY + "此世界不允许使用挑战令牌");
            return;
        }

        // 检查冷却
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && cooldowns.get(uuid) > now) {
            long remaining = (cooldowns.get(uuid) - now) / 1000;
            player.sendMessage(org.bukkit.ChatColor.GRAY + "请等待 " + remaining + " 秒后再使用");
            return;
        }

        // 检查是否已在挑战中
        if (activeChallenges.contains(uuid)) {
            player.sendMessage(org.bukkit.ChatColor.GRAY + "你已在挑战中");
            return;
        }

        // 开始挑战
        challengeManager.startChallenge(player);
        activeChallenges.add(uuid);
        int cooldownSeconds = configManager.getCooldownSeconds();
        if (cooldownSeconds > 0) {
            cooldowns.put(uuid, now + cooldownSeconds * 1000L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        // 挑战现有怪物消除由 ChallengeManager 自行处理
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        if (!activeChallenges.remove(uuid)) {
            return;
        }
        int cooldownSeconds = configManager.getCooldownSeconds();
        if (cooldownSeconds > 0) {
            cooldowns.put(uuid, System.currentTimeMillis() + cooldownSeconds * 1000L);
        }
        player.sendMessage(org.bukkit.ChatColor.AQUA + "挑战失败！");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!activeChallenges.remove(uuid)) {
            return;
        }
        int cooldownSeconds = configManager.getCooldownSeconds();
        if (cooldownSeconds > 0) {
            cooldowns.put(uuid, System.currentTimeMillis() + cooldownSeconds * 1000L);
        }
        player.sendMessage(org.bukkit.ChatColor.AQUA + "挑战因退出而失败！");
    }
}