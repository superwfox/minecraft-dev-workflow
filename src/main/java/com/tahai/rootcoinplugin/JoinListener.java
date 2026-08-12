package com.tahai.rootcoinplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class JoinListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (DataManager.getQQ(player.getUniqueId()) == null) {
            player.setMetadata("rootcoin_bound", new FixedMetadataValue(
                    Bukkit.getPluginManager().getPlugin("RootCoinPlugin"), false));
            player.sendMessage(ChatColor.GRAY + "你尚未绑定QQ，请使用 /bind 完成绑定。");
        }
    }
}