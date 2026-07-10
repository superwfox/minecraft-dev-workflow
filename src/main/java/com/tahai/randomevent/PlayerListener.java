package com.tahai.randomevent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final EventTaskManager eventTaskManager;

    public PlayerListener(EventTaskManager eventTaskManager) {
        this.eventTaskManager = eventTaskManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        eventTaskManager.forceTrigger(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 退出时无需额外操作，当前 API 中 EventTaskManager 未提供单个玩家取消任务的方法
    }
}