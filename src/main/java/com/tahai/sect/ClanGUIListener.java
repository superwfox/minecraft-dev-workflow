package com.tahai.sect;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ClanGUIListener implements Listener {

    private final ClanManager clanManager;
    private final ClanGUI gui;

    public ClanGUIListener(ClanManager clanManager, ClanGUI gui) {
        this.clanManager = clanManager;
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ClanGUI clanGUI)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        var ctx = clanGUI.getContext(player.getUniqueId());
        if (ctx == null) return;
        String name = ChatColor.stripColor(meta.getDisplayName());
        if (name == null || name.isEmpty()) return;
        if ("LIST".equals(ctx.type.name())) {
            handleListClick(player, name);
        } else if ("MANAGE".equals(ctx.type.name())) {
            handleManageClick(player, item, name);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // 上下文由 ClanGUI 在玩家下次打开时重建
    }

    private void handleListClick(Player player, String clanName) {
        String own = clanManager.getClanName(player.getUniqueId());
        if (own == null) {
            clanManager.requestJoin(player, clanName);
        } else if (own.equals(clanName)) {
            gui.openManage(player);
        } else {
            clanManager.inviteWar(player, clanName);
        }
    }

    private void handleManageClick(Player player, ItemStack item, String name) {
        if (name.contains("邀请")) {
            player.sendMessage(ChatColor.GRAY + "宗门邀请功能暂未开放，请让玩家从宗门列表申请加入");
        } else if (name.contains("提升")) {
            clanManager.promote(player);
        } else if (name.contains("升级")) {
            clanManager.upgradeClan(player);
        } else if (name.contains("宗门战")) {
            gui.openList(player, 0);
            return;
        } else if (name.contains("解散")) {
            clanManager.disbandClan(player);
        } else if (name.contains("审批")) {
            List<String> lore = item.getLore();
            if (lore != null && !lore.isEmpty()) {
                String target = ChatColor.stripColor(lore.get(0));
                if (target != null && !target.isEmpty()) {
                    clanManager.approveJoin(player, target);
                }
            } else {
                player.sendMessage(ChatColor.GRAY + "当前没有待审批的申请");
            }
        }
        gui.refresh(player);
    }
}