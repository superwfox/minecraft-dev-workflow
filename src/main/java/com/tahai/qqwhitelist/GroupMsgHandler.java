package com.tahai.qqwhitelist;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupMsgHandler {

    private static final Pattern CODE_PATTERN = Pattern.compile("\\b(\\d{6})\\b");

    public static void handle(String userQQ, String msg) {
        msg = msg.trim();
        if (msg.isEmpty()) return;

        Matcher matcher = CODE_PATTERN.matcher(msg);
        if (!matcher.find()) return;

        String code = matcher.group(1);
        String playerName = VerificationManager.verifyCode(code);
        if (playerName == null) {
            OneBotApi.sendG("验证码无效或已过期，请重试。");
            return;
        }

        VerificationManager.deleteCode(code);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("QqWhitelist");
        if (plugin == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + playerName);
                if (success) {
                    OneBotApi.sendG("验证成功，已为 " + playerName + " 添加白名单。");
                } else {
                    OneBotApi.sendG("白名单添加失败，请联系管理员。");
                }
            }
        }.runTask(plugin);
    }
}