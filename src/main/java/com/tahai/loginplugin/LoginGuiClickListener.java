package com.tahai.loginplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LoginGuiClickListener implements Listener {

    private final LoginUtil loginUtil;

    public LoginGuiClickListener(LoginUtil loginUtil) {
        this.loginUtil = loginUtil;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof LoginGuiHolder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getRawSlot() != 2) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        if (result == null || !result.hasItemMeta()) {
            return;
        }

        ItemMeta meta = result.getItemMeta();
        if (!meta.hasDisplayName()) {
            return;
        }

        String password = meta.getDisplayName();
        Player player = (Player) event.getWhoClicked();
        if (password.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "密码不能为空");
            return;
        }

        if (loginUtil.getPassword(player) != null) {
            login(player, password);
        } else {
            register(player, password);
        }
    }

    private void login(Player player, String password) {
        String stored = loginUtil.getPassword(player);
        if (loginUtil.sha256(password).equals(stored)) {
            loginUtil.setLoggedIn(player, true);
            loginUtil.setErrorCount(player, 0);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "登录成功" + ChatColor.YELLOW + "，欢迎回来！");
        } else {
            int errors = loginUtil.getErrorCount(player) + 1;
            loginUtil.setErrorCount(player, errors);
            if (errors >= 3) {
                loginUtil.setErrorCount(player, 0);
                player.kickPlayer(ChatColor.AQUA + "登录失败次数过多，已被踢出！");
            } else {
                player.sendMessage(ChatColor.AQUA + "密码错误！剩余尝试次数：" + (3 - errors));
            }
        }
    }

    private void register(Player player, String password) {
        String temp = loginUtil.getTempPassword(player);
        if (temp == null) {
            loginUtil.setTempPassword(player, password);
            player.sendMessage(ChatColor.GRAY + "请再次输入密码确认");
        } else if (password.equals(temp) || loginUtil.sha256(password).equals(temp)) {
            loginUtil.setPassword(player, password);
            loginUtil.setTempPassword(player, null);
            loginUtil.setLoggedIn(player, true);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "注册成功" + ChatColor.YELLOW + "，欢迎加入！");
        } else {
            loginUtil.setTempPassword(player, null);
            player.sendMessage(ChatColor.AQUA + "两次密码输入不一致，请重新注册");
        }
    }
}