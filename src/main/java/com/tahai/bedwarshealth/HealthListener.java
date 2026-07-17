package com.tahai.bedwarshealth;

import com.andrei1058.bedwars.api.events.GameStartedEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public class HealthListener implements Listener {

    private final Plugin plugin;

    public HealthListener(Plugin plugin) {
        this.plugin = plugin;
    }

    private void setHealth(Player player) {
        FileConfiguration config = plugin.getConfig();
        double amount = config.getDouble("health-amount", 20.0);
        player.setMaxHealth(amount);
        player.setHealth(amount);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGameStart(GameStartedEvent event) {
        for (Player player : event.getPlayers()) {
            setHealth(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        setHealth(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeaveArena(PlayerLeaveArenaEvent event) {
        Player player = event.getPlayer();
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
    }
}