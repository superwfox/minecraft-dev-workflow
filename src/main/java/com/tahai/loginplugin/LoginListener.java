package com.tahai.loginplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class LoginListener implements Listener {

    private final LoginUtil loginUtil;
    private final LoginGuiHolder guiHolder;

    public LoginListener(LoginUtil loginUtil, LoginGuiHolder guiHolder) {
        this.loginUtil = loginUtil;
        this.guiHolder = guiHolder;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String password = loginUtil.getPassword(player);
        if (password == null || password.isEmpty()) {
            guiHolder.openRegister(player);
        } else {
            guiHolder.openLogin(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!loginUtil.isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!loginUtil.isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!loginUtil.isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!loginUtil.isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof LoginGuiHolder)) {
            return;
        }
        if (event.getPlayer() instanceof Player player && !loginUtil.isLoggedIn(player)) {
            String password = loginUtil.getPassword(player);
            if (password == null || password.isEmpty()) {
                guiHolder.openRegister(player);
            } else {
                guiHolder.openLogin(player);
            }
        }
    }
}