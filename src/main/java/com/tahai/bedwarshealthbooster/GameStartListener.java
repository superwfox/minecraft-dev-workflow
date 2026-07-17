package com.tahai.bedwarshealthbooster;

import com.tomkeuper.bedwars.api.events.game.GameStartedEvent;
import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GameStartListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGameStart(GameStartedEvent event) {
        IArena arena = event.getArena();
        if (arena == null) return;
        for (Player player : arena.getPlayers()) {
            if (player == null || !player.isOnline()) continue;
            AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(40.0);
            }
            player.setHealth(40.0);
        }
    }
}