package com.tahai.randomevent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class GroupMsgHandler {

    private static EventTaskManager taskManager;
    private static boolean paused = false;

    private GroupMsgHandler() {}

    /**
     * 由插件主类在启用时调用，注入 EventTaskManager 实例。
     */
    public static void setEventTaskManager(EventTaskManager manager) {
        taskManager = manager;
    }

    public static void handle(String userQQ, String msg) {
        msg = msg.trim();
        if (msg.isEmpty()) return;

        String[] parts = msg.split("\\s+", 3);
        String cmd = parts[0].toLowerCase();
        if (!cmd.equals("/revent") && !cmd.equals("revent")) {
            return;
        }
        if (parts.length < 2) {
            OneBotApi.sendG("用法: /revent status|force <玩家>|pause|resume|broadcast <消息>");
            return;
        }
        String sub = parts[1].toLowerCase();
        switch (sub) {
            case "status" -> {
                String status = paused ? "定时事件已暂停" : "定时事件正在运行";
                OneBotApi.sendG("当前事件状态: " + status);
            }
            case "force" -> {
                if (taskManager == null) {
                    OneBotApi.sendG("事件管理器未就绪，请稍后再试");
                    return;
                }
                if (parts.length < 3) {
                    OneBotApi.sendG("请指定玩家名: /revent force <玩家>");
                    return;
                }
                String playerName = parts[2];
                Player player = Bukkit.getPlayerExact(playerName);
                if (player == null || !player.isOnline()) {
                    OneBotApi.sendG("玩家 " + playerName + " 不在线或不存在");
                    return;
                }
                taskManager.forceTrigger(player.getUniqueId());
                OneBotApi.sendG("已强制对 " + playerName + " 触发事件");
            }
            case "pause" -> {
                if (taskManager == null) {
                    OneBotApi.sendG("事件管理器未就绪，请稍后再试");
                    return;
                }
                taskManager.pause();
                paused = true;
                OneBotApi.sendG("定时事件已暂停");
            }
            case "resume" -> {
                if (taskManager == null) {
                    OneBotApi.sendG("事件管理器未就绪，请稍后再试");
                    return;
                }
                taskManager.resume();
                paused = false;
                OneBotApi.sendG("定时事件已恢复");
            }
            case "broadcast" -> {
                if (parts.length < 3 || parts[2].isEmpty()) {
                    OneBotApi.sendG("请输入广播内容: /revent broadcast <消息>");
                    return;
                }
                OneBotApi.sendG(parts[2]);
            }
            default ->
                OneBotApi.sendG("未知子命令，可用: status, force <玩家>, pause, resume, broadcast <消息>");
        }
    }
}