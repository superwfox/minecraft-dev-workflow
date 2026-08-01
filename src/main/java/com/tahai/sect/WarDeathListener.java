package com.tahai.sect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarDeathListener implements Listener {

    private final SectDataManager dataManager;
    private final Map<String, Integer> scores = new HashMap<>();

    public WarDeathListener(SectDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Object war = dataManager.getCurrentWar();
        if (war == null) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player victim)) {
            return;
        }
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }

        String attacker = getFieldString(war, "attacker");
        String defender = getFieldString(war, "defender");
        if (attacker == null || defender == null) {
            return;
        }

        String killerSect = getSectName(killer.getUniqueId());
        String victimSect = getSectName(victim.getUniqueId());
        if (killerSect == null || victimSect == null) {
            return;
        }
        if (!killerSect.equals(attacker) && !killerSect.equals(defender)) {
            return;
        }
        if (!victimSect.equals(attacker) && !victimSect.equals(defender)) {
            return;
        }
        if (killerSect.equals(victimSect)) {
            return;
        }

        scores.merge(killerSect, 1, Integer::sum);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "宗门战比分 " + attacker + " "
                + scores.getOrDefault(attacker, 0) + " : "
                + scores.getOrDefault(defender, 0) + " " + defender
                + " (" + killer.getName() + " 击杀了 " + victim.getName() + ")");
    }

    private String getSectName(UUID playerId) {
        for (Map.Entry<String, ?> entry : dataManager.getSects().entrySet()) {
            Object members = getFieldValue(entry.getValue(), "members");
            if (members instanceof Iterable<?> iterable) {
                for (Object member : iterable) {
                    if (member.toString().equalsIgnoreCase(playerId.toString())) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    private String getFieldString(Object obj, String name) {
        Object value = getFieldValue(obj, name);
        return value == null ? null : value.toString();
    }

    private Object getFieldValue(Object obj, String name) {
        if (obj instanceof Map<?, ?> map) {
            return map.get(name);
        }
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}