package com.tahai.fakeplayer;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;

public class FakePlayerListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        FakePlayerManager manager = new FakePlayerManager();
        for (Map.Entry<String, Entity> entry : manager.getFakePlayers().entrySet()) {
            if (entry.getValue().getUniqueId().equals(entity.getUniqueId())) {
                manager.removeFakePlayer(entry.getKey());
                manager.clearChunkTickets();
                break;
            }
        }
    }
}