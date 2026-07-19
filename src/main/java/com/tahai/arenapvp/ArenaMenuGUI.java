package com.tahai.arenapvp;

import com.tahai.arenapvp.ArenaManager;
import com.tahai.arenapvp.ArenaManager.Game;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArenaMenuGUI implements InventoryHolder, Listener {

    private final ArenaManager arenaManager;
    private Inventory mainMenu;
    private Inventory leaderboard;

    public ArenaMenuGUI(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    public void openMainMenu(Player player) {
        int size = 27;
        String title = "竞技场主菜单";
        mainMenu = Bukkit.createInventory(this, size, title);

        Collection<Game> games = arenaManager.getGames();
        int slot = 0;
        for (Game game : games) {
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(game.getName());
            List<String> lore = new ArrayList<>();
            lore.add("状态: " + game.getState().name());
            lore.add("玩家: " + game.getPlayerCount() + "/" + game.getPlayers().size());
            lore.add("世界: " + game.getWorld().getName());
            meta.setLore(lore);
            item.setItemMeta(meta);
            mainMenu.setItem(slot++, item);
            if (slot >= 27) break;
        }

        player.openInventory(mainMenu);
    }

    public void openLeaderboard(Player player) {
        int size = 27;
        String title = "排行榜";
        leaderboard = Bukkit.createInventory(this, size, title);

        // 简单展示占位信息（实际可扩展从 data.yml 读取所有玩家统计数据）
        ItemStack placeholder = new ItemStack(Material.PAPER);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName("排行榜");
        List<String> lore = new ArrayList<>();
        lore.add("功能开发中…");
        lore.add("当前在线玩家数: " + Bukkit.getOnlinePlayers().size());
        meta.setLore(lore);
        placeholder.setItemMeta(meta);
        leaderboard.setItem(13, placeholder);

        player.openInventory(leaderboard);
    }

    @Override
    public Inventory getInventory() {
        return mainMenu != null ? mainMenu : Bukkit.createInventory(this, 9, "空");
    }
}