package com.tahai.qqwhitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.security.SecureRandom;

public class JoinListener implements Listener {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 检查原版白名单
        if (Bukkit.getWhitelist().isWhitelisted(player.getUniqueId())) {
            return;
        }

        String name = player.getName();
        VerificationManager vm = new VerificationManager(); // 需要获取实例？实际 VerificationManager 是静态方法，不需要实例化

        if (vm.hasUnexpiredCode(name)) {
            // 已有未过期验证码
            String kickMsg = ChatColor.RED + "你已有未使用的验证码，请查看QQ消息或在群内输入 /code 获取。";
            player.kickPlayer(kickMsg);
        } else {
            // 生成新验证码
            String code = generateCode();
            vm.addCode(code, name, System.currentTimeMillis());

            String groupId = ConfigManager.GroupId;
            String kickMsg = ChatColor.GOLD + "请加入QQ群 " + groupId + " 并发送验证码: " + ChatColor.GREEN + code;
            player.kickPlayer(kickMsg);
        }
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}