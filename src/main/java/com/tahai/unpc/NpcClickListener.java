package com.tahai.unpc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class NpcClickListener implements Listener {

    private static final String NPC_METADATA_KEY = "unpc_npc";

    private final NpcManager npcManager;

    public NpcClickListener(NpcManager npcManager) {
        this.npcManager = npcManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        NpcData data = getNpcData(entity);
        if (data == null) {
            return;
        }

        event.setCancelled(true);

        String command = data.getCommand();
        if (command == null || command.isEmpty()) {
            return;
        }

        if ("console".equalsIgnoreCase(data.getCommandExecutor())) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Player player = event.getPlayer();
            Bukkit.dispatchCommand(player, command);
        }
    }

    private NpcData getNpcData(Entity entity) {
        if (entity == null || !entity.hasMetadata(NPC_METADATA_KEY)) {
            return null;
        }

        List<MetadataValue> metadata = entity.getMetadata(NPC_METADATA_KEY);
        if (metadata.isEmpty()) {
            return null;
        }

        Object value = metadata.get(0).value();
        return value instanceof NpcData ? (NpcData) value : null;
    }
}