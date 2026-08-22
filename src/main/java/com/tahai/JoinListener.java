package com.tahai.laowu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 预留登录监听文件，插件无登录逻辑，保留空白不新增/删除文件
 */
public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // 预留扩展位，当前无功能
    }
}
