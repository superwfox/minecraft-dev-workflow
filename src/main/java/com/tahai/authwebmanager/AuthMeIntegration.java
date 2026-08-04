package com.tahai.authwebmanager;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class AuthMeIntegration {

    private final AuthMeApi authMeApi;

    public AuthMeIntegration() {
        RegisteredServiceProvider<AuthMeApi> rsp = Bukkit.getServicesManager().getRegistration(AuthMeApi.class);
        if (rsp == null) {
            throw new IllegalStateException("AuthMe API not found");
        }
        this.authMeApi = rsp.getProvider();
    }

    /**
     * 验证玩家密码
     */
    public boolean checkPassword(Player player, String password) {
        return authMeApi.checkPassword(player.getName(), password);
    }

    /**
     * 修改玩家密码
     */
    public void changePassword(Player player, String newPassword) {
        authMeApi.changePassword(player.getName(), newPassword);
    }

    /**
     * 删除玩家账户
     */
    public void deletePlayer(Player player) {
        authMeApi.forceUnregister(player);
    }
}