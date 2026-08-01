package com.tahai.sect;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final ChatListener chatListener;

    public InventoryClickListener(ChatListener chatListener) {
        this.chatListener = chatListener;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SectGUI gui)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        GuiType type = gui.getType();
        int slot = event.getRawSlot();

        if (type == GuiType.MAIN_MENU) {
            handleMainMenu(player, gui, slot);
        } else if (type == GuiType.JOIN_LIST) {
            handleJoinList(player, gui, slot);
        } else if (type == GuiType.CONFIRM_DELETE) {
            handleConfirmDelete(player, gui, slot);
        } else if (type == GuiType.WAR_INVITE) {
            handleWarInvite(player, gui, slot);
        }
    }

    private void handleMainMenu(Player player, SectGUI gui, int slot) {
        String sectName = getPlayerSect(gui.getDataManager(), player);
        if (sectName == null) {
            player.sendMessage(ChatColor.AQUA + "你不在任何宗门中。");
            return;
        }

        if (slot == 0) {
            chatListener.setPendingInvite(player.getUniqueId(), sectName);
            player.sendMessage(ChatColor.GRAY + "请在聊天栏输入要邀请的玩家名称。");
            player.closeInventory();
        } else if (slot == 1) {
            if (gui.getDataManager().upgradeSect(sectName, player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "宗门升级成功！");
            } else {
                player.sendMessage(ChatColor.AQUA + "宗门升级失败。");
            }
        } else if (slot == 2) {
            gui.openConfirmDelete(player, gui.getDataManager(), sectName);
        } else if (slot == 3) {
            gui.openJoinList(player, gui.getDataManager(), 1);
        }
    }

    private void handleJoinList(Player player, SectGUI gui, int slot) {
        String target = gui.getTargetSect();
        if (target == null || target.isEmpty()) {
            return;
        }

        if (gui.getDataManager().isInAnySect(player)) {
            String playerSect = getPlayerSect(gui.getDataManager(), player);
            if (playerSect != null && !playerSect.equalsIgnoreCase(target)) {
                gui.openWarInvite(player, gui.getDataManager(), playerSect, target);
            }
        } else {
            if (gui.getDataManager().acceptInvite(target, player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "成功加入宗门 " + target + " ！");
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.AQUA + "加入宗门失败。");
            }
        }
    }

    private void handleConfirmDelete(Player player, SectGUI gui, int slot) {
        String sectName = gui.getTargetSect();
        if (sectName == null || sectName.isEmpty()) {
            return;
        }

        if (slot == 4) {
            if (gui.getDataManager().disbandSect(sectName, player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "宗门已解散。");
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.AQUA + "解散宗门失败。");
            }
        } else if (slot == 8) {
            player.closeInventory();
        }
    }

    private void handleWarInvite(Player player, SectGUI gui, int slot) {
        String attacker = gui.getAttackerSect();
        String defender = gui.getDefenderSect();
        if (attacker == null || defender == null) {
            return;
        }

        if (slot == 3) {
            if (gui.getDataManager().startWar(attacker, defender, player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "战争请求已发送。");
            } else {
                player.sendMessage(ChatColor.AQUA + "无法发起战争。");
            }
        } else if (slot == 5) {
            if (gui.getDataManager().acceptWar(defender, player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "你已接受战争！");
            } else {
                player.sendMessage(ChatColor.AQUA + "无法接受战争。");
            }
        }
    }

    private String getPlayerSect(SectDataManager dataManager, Player player) {
        for (Sect sect : dataManager.getSects().values()) {
            if (sect.isMember(player.getUniqueId())) {
                return sect.getName();
            }
        }
        return null;
    }
}