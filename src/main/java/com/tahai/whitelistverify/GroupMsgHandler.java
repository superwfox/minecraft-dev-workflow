package com.tahai.whitelistverify;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class GroupMsgHandler {

    public static void handle(String userQQ, String msg) {
        msg = msg.trim();
        String[] parts = msg.split("\\s+");
        if (parts.length != 3 || !parts[0].equals("绑定")) return;

        String playerName = parts[1];
        String code = parts[2];

        // 检查验证码是否存在
        JsonObject data = DataManager.find(code);
        if (data == null) {
            OneBotApi.sendG("验证码无效，请重新申请。");
            return;
        }

        String storedPlayer = data.get("playerName").getAsString();
        if (!storedPlayer.equals(playerName)) {
            OneBotApi.sendG("玩家名与验证码不匹配，请检查输入。");
            return;
        }

        // 验证通过，在主线程执行添加白名单
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WhiteListVerify");
        if (plugin == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + playerName);
            DataManager.remove(code);
            DataManager.save();
            OneBotApi.sendG("验证成功，已为 §a" + playerName + " §r添加白名单！");
        });
    }
}