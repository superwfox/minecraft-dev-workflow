package com.tahai.customhud;

import org.bukkit.entity.Player;

public class ResourcePackService {

    public void sendResourcePack(Player player, String url, byte[] hash) {
        player.setResourcePack(url, hash);
    }

    public void sendResourcePack(Player player, String url) {
        player.setResourcePack(url);
    }
}