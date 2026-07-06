package com.tahai.spaccessory;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class PermissionManager {

    private final Plugin plugin;
    private final Map<Player, PermissionAttachment> attachments = new HashMap<>();

    public PermissionManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void addPermission(Player player, String permission) {
        PermissionAttachment attachment = attachments.get(player);
        if (attachment == null) {
            attachment = player.addAttachment(plugin);
            attachments.put(player, attachment);
        }
        attachment.setPermission(permission, true);
    }

    public void removePermission(Player player, String permission) {
        PermissionAttachment attachment = attachments.get(player);
        if (attachment != null) {
            attachment.setPermission(permission, false);
        }
    }
}