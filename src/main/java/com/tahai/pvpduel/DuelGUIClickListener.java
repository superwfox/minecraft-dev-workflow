package com.tahai.pvpduel;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class DuelGUIClickListener implements Listener {
    private final DuelManager duelManager;

    public DuelGUIClickListener(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DuelGUI)) {
            return;
        }
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getInventory().getSize()) {
            return;
        }

        Player inviter = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() != Material.PLAYER_HEAD || !(item.getItemMeta() instanceof SkullMeta)) {
            inviter.closeInventory();
            return;
        }

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        OfflinePlayer target = meta.getOwningPlayer();

        if (target == null || !target.isOnline()) {
            inviter.closeInventory();
            return;
        }

        UUID inviterId = inviter.getUniqueId();

        if (duelManager.isInDuel(inviterId)) {
            inviter.sendMessage(ChatColor.AQUA + "你已经在决斗中！");
            inviter.closeInventory();
            return;
        }

        if (!duelManager.invite(inviterId, target.getUniqueId())) {
            inviter.sendMessage(ChatColor.AQUA + "无法发送决斗邀请，你可能已有待处理邀请。");
            inviter.closeInventory();
            return;
        }

        Player targetPlayer = target.getPlayer();
        if (targetPlayer == null) {
            inviter.closeInventory();
            return;
        }

        targetPlayer.sendMessage(ChatColor.GRAY + inviter.getName() + ChatColor.YELLOW + " 向你发起了决斗邀请！使用 /accept 接受。");
        inviter.sendMessage(ChatColor.YELLOW + "已向 " + targetPlayer.getName() + " 发送决斗邀请。");
        inviter.closeInventory();
    }
}