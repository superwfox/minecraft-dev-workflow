package com.tahai.pvpduel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class InviteExpireTask extends BukkitRunnable {

    private static final long EXPIRE_MILLIS = 15000L;

    private final DuelManager duelManager;
    private final Map<UUID, Long> observedTimes = new HashMap<>();

    public InviteExpireTask(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public void run() {
        Map<UUID, Object> invites = findInviteMap();
        if (invites == null) {
            observedTimes.clear();
            return;
        }

        long now = System.currentTimeMillis();
        Set<UUID> current = new HashSet<>(invites.keySet());
        observedTimes.keySet().removeIf(uuid -> !current.contains(uuid));

        for (UUID invitee : current) {
            observedTimes.putIfAbsent(invitee, now);
        }

        for (UUID invitee : new ArrayList<>(observedTimes.keySet())) {
            long start = observedTimes.get(invitee);
            if (now - start < EXPIRE_MILLIS) {
                continue;
            }

            observedTimes.remove(invitee);
            Object invite = invites.get(invitee);
            UUID inviter = invite == null ? null : findInviter(invite, invitee);

            if (!duelManager.removeInvite(invitee)) {
                continue;
            }

            Player inviterPlayer = inviter == null ? null : Bukkit.getPlayer(inviter);
            Player inviteePlayer = Bukkit.getPlayer(invitee);

            if (inviterPlayer != null) {
                inviterPlayer.sendMessage(ChatColor.AQUA + "发送给 " + (inviteePlayer == null ? "离线玩家" : inviteePlayer.getName()) + " 的决斗邀请已过期。");
            }
            if (inviteePlayer != null) {
                inviterPlayer = inviterPlayer == null && inviter != null ? Bukkit.getPlayer(inviter) : inviterPlayer;
                inviterPlayer = inviterPlayer == null ? Bukkit.getPlayer(inviter) : inviterPlayer;
                String name = inviterPlayer == null ? "未知玩家" : inviterPlayer.getName();
                inviteePlayer.sendMessage(ChatColor.AQUA + "来自 " + name + " 的决斗邀请已过期。");
            }
        }
    }

    private Map<UUID, Object> findInviteMap() {
        for (Field field : DuelManager.class.getDeclaredFields()) {
            if (!Map.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(duelManager);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (!(value instanceof Map)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty()) {
                continue;
            }
            Object key = map.keySet().iterator().next();
            if (!(key instanceof UUID)) {
                continue;
            }
            Object val = map.values().iterator().next();
            if (val != null && !(val instanceof UUID) && !(val instanceof Player) && !(val instanceof Long)) {
                @SuppressWarnings("unchecked")
                Map<UUID, Object> casted = (Map<UUID, Object>) map;
                return casted;
            }
        }
        return null;
    }

    private UUID findInviter(Object invite, UUID invitee) {
        for (Field field : invite.getClass().getDeclaredFields()) {
            if (field.getType() != UUID.class) {
                continue;
            }
            field.setAccessible(true);
            try {
                UUID uuid = (UUID) field.get(invite);
                if (uuid != null && !uuid.equals(invitee)) {
                    return uuid;
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return null;
    }
}