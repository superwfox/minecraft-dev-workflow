package com.tahai.authlogin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class JoinListener implements Listener {

    private static final String METADATA_KEY = "authlogin_logged_in";

    private final DataManager dataManager;
    private final Plugin plugin;

    public JoinListener(DataManager dataManager) {
        this.dataManager = dataManager;
        this.plugin = Bukkit.getPluginManager().getPlugin("AuthLogin");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String title = dataManager.hasPassword(player.getUniqueId()) ? "登录" : "注册";
        new AuthGui().open(player, title);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.removeMetadata(METADATA_KEY, plugin);
    }

    private static class AuthGui implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        void open(Player player, String title) {
            inventory = Bukkit.createInventory(this, 9, title);
            player.openInventory(inventory);
        }
    }
}