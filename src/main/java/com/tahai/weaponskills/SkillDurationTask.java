package com.tahai.weaponskills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SkillDurationTask extends BukkitRunnable {

    private final UUID playerUuid;

    public SkillDurationTask(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.GRAY + "你的剑技能效果已结束。");
        }
    }
}