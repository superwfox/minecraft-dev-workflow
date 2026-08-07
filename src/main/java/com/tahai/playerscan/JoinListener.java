package com.tahai.playerscan;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class JoinListener implements Listener {

    private final IPHistoryStorage ipHistoryStorage;

    public JoinListener(IPHistoryStorage ipHistoryStorage) {
        this.ipHistoryStorage = ipHistoryStorage;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String name = player.getName();
        String ip = "unknown";
        if (player.getAddress() != null && player.getAddress().getAddress() != null) {
            ip = player.getAddress().getAddress().getHostAddress();
        }

        Map<String, PlayerRecord> records = ipHistoryStorage.getRecords();
        records.compute(uuid, (k, r) -> {
            PlayerRecord record;
            if (r == null) {
                record = new PlayerRecord(name);
            } else {
                record = r;
                // 无法更新玩家名称，保留原有名称
            }
            record.addIp(ip);
            return record;
        });
        ipHistoryStorage.saveAsync();
    }
}