package com.tahai.sect;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class ChatListener implements Listener {

    private static final Map<UUID, String> PENDING_INVITES = new ConcurrentHashMap<>();
    private final SectDataManager dataManager;

    public ChatListener(SectDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public static void setPendingInvite(UUID playerId, String sectName) {
        PENDING_INVITES.put(playerId, sectName);
    }

    public static void removePendingInvite(UUID playerId) {
        PENDING_INVITES.remove(playerId);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String sectName = PENDING_INVITES.get(player.getUniqueId());
        if (sectName == null) {
            return;
        }
        event.setCancelled(true);
        String targetName = event.getMessage().trim();
        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetId = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            PENDING_INVITES.remove(player.getUniqueId());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean success = dataManager.invitePlayer(sectName, player.getUniqueId(), targetId);
            if (success) {
                player.sendMessage(ChatColor.YELLOW + "已向 " + targetName + " 发出宗门邀请");
            } else {
                player.sendMessage(ChatColor.AQUA + "邀请失败，请检查宗门名称或该玩家是否已在其他宗门");
            }
            PENDING_INVITES.remove(player.getUniqueId());
        });
    }
}