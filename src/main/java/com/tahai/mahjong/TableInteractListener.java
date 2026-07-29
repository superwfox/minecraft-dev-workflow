package com.tahai.mahjong;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class TableInteractListener implements Listener {
    private final GameManager gameManager;

    public TableInteractListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        event.setCancelled(true);

        String name = entity.getName();
        if (!name.startsWith("mahjong_")) return;

        String[] parts = name.split("_");
        if (parts.length < 3) return;

        String type = parts[1];
        String tableId = parts[2];

        switch (type) {
            case "seat":
                handleSeatClick(player, tableId);
                break;
            case "tile":
                if (parts.length >= 4) {
                    String tile = parts[3];
                    handleTileClick(player, tableId, tile);
                }
                break;
            case "button":
                if (parts.length >= 5) {
                    String action = parts[3];
                    String tile = parts[4];
                    handleButtonClick(player, tableId, action, tile);
                }
                break;
            case "table":
                handleTableClick(player, tableId);
                break;
            default:
                break;
        }
    }

    private void handleSeatClick(Player player, String tableId) {
        if (gameManager.joinTable(tableId, player)) {
            player.sendMessage(ChatColor.YELLOW + "你加入了牌桌 " + tableId);
        } else {
            if (gameManager.setReady(tableId, player)) {
                player.sendMessage(ChatColor.GRAY + "你已准备就绪");
            } else {
                player.sendMessage(ChatColor.AQUA + "无法加入或准备，可能桌已满或游戏已开始");
            }
        }
    }

    private void handleTileClick(Player player, String tableId, String tile) {
        if (gameManager.playerPlayTile(tableId, player, tile)) {
            player.sendMessage(ChatColor.GRAY + "你打出了 " + tile);
        } else {
            player.sendMessage(ChatColor.AQUA + "出牌失败，可能不是你的回合");
        }
    }

    private void handleButtonClick(Player player, String tableId, String action, String tile) {
        if (gameManager.playerAction(tableId, player, action, tile)) {
            player.sendMessage(ChatColor.GRAY + "执行了 " + action + " " + tile);
        } else {
            player.sendMessage(ChatColor.AQUA + "操作失败");
        }
    }

    private void handleTableClick(Player player, String tableId) {
        if (gameManager.startGame(tableId)) {
            player.sendMessage(ChatColor.YELLOW + "游戏已开始");
        } else {
            player.sendMessage(ChatColor.AQUA + "无法开始游戏，可能人数不足或你不是房主");
        }
    }
}