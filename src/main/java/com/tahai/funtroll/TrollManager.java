package com.tahai.funtroll;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TrollManager implements Listener {

    private final Plugin plugin;
    private final Map<Player, Map<TrollType, BukkitRunnable>> activeTrolls = new HashMap<>();
    private final Map<Player, Float> originalWalkSpeed = new HashMap<>();

    public TrollManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public enum TrollType {
        FREEZE,
        ANVIL,
        LIGHTNING
    }

    public void startTroll(Player player, String typeStr) {
        TrollType type;
        try {
            type = TrollType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid troll type: " + typeStr);
            return;
        }

        stopCurrentTroll(player, type);

        Map<TrollType, BukkitRunnable> playerTrolls = activeTrolls.computeIfAbsent(player, k -> new HashMap<>());

        switch (type) {
            case FREEZE:
                startFreeze(player, playerTrolls);
                break;
            case ANVIL:
                startAnvil(player, playerTrolls);
                break;
            case LIGHTNING:
                startLightning(player, playerTrolls);
                break;
        }
    }

    public void stopAllTrolls(Player player) {
        Map<TrollType, BukkitRunnable> playerTrolls = activeTrolls.remove(player);
        if (playerTrolls != null) {
            for (BukkitRunnable task : playerTrolls.values()) {
                if (task != null) {
                    task.cancel();
                }
            }
            restoreWalkSpeed(player);
        }
    }

    public void stopAllTrolls() {
        for (Player player : new HashMap<>(activeTrolls).keySet()) {
            stopAllTrolls(player);
        }
    }

    public void save() {
        // 无持久数据，空方法
    }

    public void shutdown() {
        stopAllTrolls();
    }

    private void stopCurrentTroll(Player player, TrollType type) {
        Map<TrollType, BukkitRunnable> playerTrolls = activeTrolls.get(player);
        if (playerTrolls != null) {
            BukkitRunnable oldTask = playerTrolls.remove(type);
            if (oldTask != null) {
                oldTask.cancel();
                if (type == TrollType.FREEZE) {
                    restoreWalkSpeed(player);
                }
            }
            if (playerTrolls.isEmpty()) {
                activeTrolls.remove(player);
            }
        }
    }

    private void startFreeze(Player player, Map<TrollType, BukkitRunnable> playerTrolls) {
        originalWalkSpeed.put(player, player.getWalkSpeed());
        player.setWalkSpeed(0f);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // 空任务，仅作为占位符
            }
        };
        playerTrolls.put(TrollType.FREEZE, task);
    }

    private void startAnvil(Player player, Map<TrollType, BukkitRunnable> playerTrolls) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopCurrentTroll(player, TrollType.ANVIL);
                    return;
                }
                player.getWorld().spawnFallingBlock(player.getLocation().add(0, 10, 0),
                        org.bukkit.Material.ANVIL, (byte) 0);
            }
        };
        task.runTaskTimer(plugin, 0L, 60L);
        playerTrolls.put(TrollType.ANVIL, task);
    }

    private void startLightning(Player player, Map<TrollType, BukkitRunnable> playerTrolls) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopCurrentTroll(player, TrollType.LIGHTNING);
                    return;
                }
                player.getWorld().strikeLightning(player.getLocation());
            }
        };
        task.runTaskTimer(plugin, 0L, 80L);
        playerTrolls.put(TrollType.LIGHTNING, task);
    }

    private void restoreWalkSpeed(Player player) {
        Float original = originalWalkSpeed.remove(player);
        if (original != null && player.isOnline()) {
            player.setWalkSpeed(original);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Map<TrollType, BukkitRunnable> playerTrolls = activeTrolls.get(player);
        if (playerTrolls != null && playerTrolls.containsKey(TrollType.FREEZE)) {
            event.setCancelled(true);
        }
    }
}