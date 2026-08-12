package com.tahai.unpc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class NpcInteractListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity == null) {
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Unpc");
        if (plugin == null) {
            return;
        }

        NpcManager npcManager = NpcManager.fromPlugin(plugin);
        NpcData data = npcManager.getNpc(entity.getName());
        if (data == null) {
            return;
        }

        if (!data.getEntityId().equals(entity.getUniqueId())) {
            return;
        }

        Player player = event.getPlayer();
        List<String> commands = data.getCommands();
        if (commands == null) {
            return;
        }

        for (String command : commands) {
            if (command == null || command.isEmpty()) {
                continue;
            }
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            Bukkit.dispatchCommand(player, command);
        }
    }
}