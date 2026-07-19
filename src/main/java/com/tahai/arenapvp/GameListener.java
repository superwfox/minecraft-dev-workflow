package com.tahai.arenapvp;

import com.tahai.arenapvp.ArenaManager.Game;
import com.tahai.arenapvp.ArenaManager.Game.GameState;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GameListener implements Listener {
    private final ArenaManager arenaManager;

    public GameListener(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        for (Game game : arenaManager.getGames()) {
            if (game.getPlayers().contains(victim.getUniqueId())) {
                game.onPlayerDeath(victim, killer);

                event.setKeepLevel(true);
                event.setDroppedExp(0);
                event.getDrops().clear();

                victim.setGameMode(GameMode.SPECTATOR);

                if (killer != null) {
                    killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }

                event.setDeathMessage(null);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        for (Game game : arenaManager.getGames()) {
            if (game.getPlayers().contains(player.getUniqueId())) {
                if (game.getState() == GameState.ACTIVE) {
                    Material type = event.getBlock().getType();
                    if (type.toString().contains("BED")) {
                        game.onBedBreak(player);
                    }
                } else {
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        for (Game game : arenaManager.getGames()) {
            if (game.getPlayers().contains(player.getUniqueId())) {
                if (game.getState() != GameState.ACTIVE) {
                    event.setCancelled(true);
                }
                break;
            }
        }
    }
}