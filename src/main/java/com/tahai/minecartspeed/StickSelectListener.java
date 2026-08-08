package com.tahai.minecartspeed;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class StickSelectListener implements Listener {

    private final SelectionManager selectionManager;

    public StickSelectListener(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.STICK) {
            return;
        }
        event.setCancelled(true);
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            selectionManager.setPoint(player.getUniqueId(), 0, event.getClickedBlock().getLocation());
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            selectionManager.setPoint(player.getUniqueId(), 1, event.getClickedBlock().getLocation());
        }
    }
}