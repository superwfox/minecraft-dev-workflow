package com.tahai.fakeplayerplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class FakePlayerDeathListener implements Listener {

    private final FakePlayerManager fakePlayerManager = new FakePlayerManager();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.isOnline()) {
            fakePlayerManager.removeFakePlayer(player.getName());
        }
    }
}