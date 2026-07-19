package com.tahai.arenapvp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaMenuListener implements Listener {

    private final ArenaManager arenaManager;
    private final Map<UUID, String> queuedPlayers = new HashMap<>();

    private static final String MAIN_MENU_TITLE = "Arena Menu";
    private static final String LEADERBOARD_TITLE = "Leaderboard";

    public ArenaMenuListener(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof ArenaMenuGUI)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inv.getSize()) {
            return;
        }

        ItemStack item = inv.getItem(slot);
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        String displayName = meta.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            return;
        }
        String stripped = ChatColor.stripColor(displayName);

        String title = ChatColor.stripColor(inv.getTitle());

        if (MAIN_MENU_TITLE.equals(title)) {
            handleMainMenuClick(player, slot, stripped);
        } else if (LEADERBOARD_TITLE.equals(title)) {
            handleLeaderboardClick(player, slot, stripped);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof ArenaMenuGUI) {
            // 清理队列
            String gameName = queuedPlayers.remove(player.getUniqueId());
            if (gameName != null) {
                player.sendMessage(ChatColor.GRAY + "你已退出排队。");
            }
        }
    }

    private void handleMainMenuClick(Player player, int slot, String strippedName) {
        // 假设 slot 0 = 查看排行榜，slot 4 = 加入队列（地图名从物品名获取）
        // 实际槽位根据 ArenaMenuGUI 实现调整，这里仅示例
        switch (slot) {
            case 0:
                // 打开排行榜
                ArenaMenuGUI gui = (ArenaMenuGUI) player.getOpenInventory().getTopInventory().getHolder();
                if (gui != null) {
                    gui.openLeaderboard(player);
                }
                break;
            case 4:
                // 加入队列，假设点击的物品名称为地图名
                String mapName = strippedName;
                if (mapName == null || mapName.isEmpty()) {
                    player.sendMessage(ChatColor.AQUA + "无法识别地图。");
                    return;
                }
                com.tahai.arenapvp.ArenaManager.Game game = arenaManager.getGame(mapName);
                if (game == null) {
                    player.sendMessage(ChatColor.AQUA + "该地图不存在。");
                    return;
                }
                if (game.getState() != com.tahai.arenapvp.ArenaManager.GameState.WAITING) {
                    player.sendMessage(ChatColor.AQUA + "该游戏已开始或已结束。");
                    return;
                }
                if (game.getPlayerCount() >= 2) { // 假设最大2人，实际需根据 maxPlayers 判断
                    player.sendMessage(ChatColor.AQUA + "队伍已满。");
                    return;
                }
                game.addPlayer(player);
                queuedPlayers.put(player.getUniqueId(), mapName);
                player.sendMessage(ChatColor.YELLOW + "你已加入 " + mapName + " 的队列！");
                break;
            default:
                // 其他槽位暂不处理
                break;
        }
    }

    private void handleLeaderboardClick(Player player, int slot, String strippedName) {
        // 在排行榜中点击某个条目（如玩家名称）可查看其统计
        // 假设只有 slot 0 返回主菜单
        if (slot == 0) {
            ArenaMenuGUI gui = (ArenaMenuGUI) player.getOpenInventory().getTopInventory().getHolder();
            if (gui != null) {
                gui.openMainMenu(player);
            }
        } else if (slot >= 1 && slot <= 9) {
            // 点击某个玩家条目，显示统计信息（仅示例）
            String playerName = strippedName;
            // 简单发送消息
            player.sendMessage(ChatColor.YELLOW + "查看玩家 " + playerName + " 的统计数据：");
            player.sendMessage(ChatColor.GRAY + "击杀: " + 0);
            player.sendMessage(ChatColor.GRAY + "死亡: " + 0);
            player.sendMessage(ChatColor.GRAY + "胜场: " + 0);
        }
    }
}