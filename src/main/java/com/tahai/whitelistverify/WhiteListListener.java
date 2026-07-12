package com.tahai.whitelistverify;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.concurrent.ThreadLocalRandom;

public class WhiteListListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();

        if (Bukkit.getOfflinePlayer(playerName).isWhitelisted()) {
            return;
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        DataManager dataManager = new DataManager();
        dataManager.add(playerName, code);
        dataManager.save();

        String message = ChatColor.GOLD + "请加群 716833975，验证码：" + code;
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
    }
}