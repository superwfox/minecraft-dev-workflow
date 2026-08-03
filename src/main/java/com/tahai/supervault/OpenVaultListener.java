package com.tahai.supervault;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OpenVaultListener implements Listener {

    private final PlayerVaultManager manager;

    public OpenVaultListener(PlayerVaultManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String required = ChatColor.translateAlternateColorCodes('&', "&6超级仓库");
        if (!meta.getDisplayName().equals(required)) {
            return;
        }

        PlayerVault vault = manager.getVault(player.getUniqueId());
        if (vault == null) {
            vault = manager.loadVault(player.getUniqueId());
        }
        if (vault == null) {
            return;
        }

        event.setCancelled(true);
        new VaultGuiHolder(manager, player).open(player);
    }
}