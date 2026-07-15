package com.tahai.qqgroupsync;

import com.tahai.qqgroupsync.ConfigManager;
import com.tahai.qqgroupsync.OneBotApi;
import org.bukkit.Bukkit;

import java.util.List;

public final class GroupMsgHandler {

    private GroupMsgHandler() {}

    public static void handle(String userQQ, String msg) {
        msg = msg.trim();
        if (msg.isEmpty()) return;

        long groupId = Long.parseLong(ConfigManager.GroupId);

        // 匹配 tps / 服务器状态
        if (msg.equals("tps") || msg.equals("服务器状态")) {
            double[] tps = Bukkit.getServer().getTPS();
            double tps5min = tps.length > 1 ? tps[1] : 20.0;
            double mspt = Bukkit.getServer().getAverageTickTime();
            String reply = String.format("当前TPS: %.2f, MSPT: %.2f ms", tps5min, mspt);
            OneBotApi.sendG(groupId, reply);
            return;
        }

        // 匹配 c/ 前缀
        if (msg.startsWith("c/")) {
            String command = msg.substring(2).trim();
            if (command.isEmpty()) return;

            List<String> adminQqs = ConfigManager.AdminQqs;
            if (adminQqs.contains(userQQ)) {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    OneBotApi.sendGroupAt(groupId, userQQ);
                    OneBotApi.sendG(groupId, " 命令执行成功");
                } catch (Exception e) {
                    OneBotApi.sendGroupAt(groupId, userQQ);
                    OneBotApi.sendG(groupId, " 命令执行失败: " + e.getMessage());
                }
            }
            // 非管理员静默忽略
        }
    }
}