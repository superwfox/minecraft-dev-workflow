package com.tahai.sect;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;

public class SectGuiListener implements Listener {

    private final DataManager dataManager;
    private final Map<String, String> warRequests = new HashMap<>();

    public SectGuiListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SectGui gui)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String type = gui.getType() == null ? "" : gui.getType().name();
        if ("JOIN".equals(type)) {
            handleJoinClick(gui, player, event.getRawSlot());
        } else if ("MANAGE".equals(type)) {
            handleManageClick(gui, player, event);
        }
    }

    private void handleJoinClick(SectGui gui, Player player, int slot) {
        if (slot == SectGui.PREVIOUS_PAGE_SLOT) {
            if (gui.previousPage()) gui.refresh();
            return;
        }
        if (slot == SectGui.NEXT_PAGE_SLOT) {
            if (gui.nextPage()) gui.refresh();
            return;
        }
        if (slot < 0 || slot >= SectGui.PAGE_SIZE) return;

        String sectName = gui.getSectNameAt(slot);
        if (sectName == null) return;

        SectClan clan = dataManager.getSect(sectName);
        if (clan == null) return;

        Player leader = Bukkit.getPlayer(clan.getLeaderUuid());
        if (leader != null) {
            leader.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " 申请加入你的宗门 [" + sectName + "]");
        }
        player.sendMessage(ChatColor.GRAY + "申请已发送给 [" + sectName + "] 的宗主。");
    }

    private void handleManageClick(SectGui gui, Player player, InventoryClickEvent event) {
        SectClan clan = dataManager.getSect(gui.getClanName());
        if (clan == null) return;

        if (!isMember(clan, player)) {
            player.sendMessage(ChatColor.AQUA + "你不是该宗门成员。");
            return;
        }

        int slot = event.getRawSlot();
        if (slot == SectGui.INVITE_SLOT) {
            handleInvite(clan, player);
        } else if (slot == SectGui.RANK_UP_SLOT) {
            handleRankUp(clan, player);
        } else if (slot == SectGui.LEVEL_UP_SLOT) {
            handleLevelUp(clan, player);
        } else if (slot == SectGui.WAR_SLOT) {
            handleWar(clan, player, event.getClick());
        }
    }

    private boolean isMember(SectClan clan, Player player) {
        return clan.getLeaderUuid().equals(player.getUniqueId())
                || clan.getMembers().containsKey(player.getUniqueId());
    }

    private void handleInvite(SectClan clan, Player player) {
        if (!clan.getLeaderUuid().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.AQUA + "只有宗主可以邀请玩家。");
            return;
        }

        String targetName = getItemName(player.getInventory().getItemInMainHand());
        if (targetName == null) {
            player.sendMessage(ChatColor.GRAY + "请手持玩家头颅或带有名字的物品，点击此按钮邀请对应玩家。");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.AQUA + "玩家 " + targetName + " 不在线。");
            return;
        }

        if (clan.getMembers().containsKey(target.getUniqueId()) || clan.getLeaderUuid().equals(target.getUniqueId())) {
            player.sendMessage(ChatColor.AQUA + "该玩家已在宗门中。");
            return;
        }

        if (dataManager.setRank(clan.getName(), target.getUniqueId(), SectRank.MEMBER)) {
            target.sendMessage(ChatColor.YELLOW + "你已被邀请加入宗门 [" + clan.getName() + "]。");
            player.sendMessage(ChatColor.GRAY + "已邀请 " + target.getName() + " 加入宗门。");
        } else {
            player.sendMessage(ChatColor.AQUA + "邀请失败，请稍后再试。");
        }
    }

    private void handleRankUp(SectClan clan, Player player) {
        if (clan.getLeaderUuid().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.AQUA + "宗主无需升级职位。");
            return;
        }

        SectRank current = clan.getMembers().get(player.getUniqueId());
        if (current == null) return;

        double cost;
        SectRank next;
        if (current == SectRank.MEMBER) {
            cost = 1000;
            next = SectRank.ELITE;
        } else if (current == SectRank.ELITE) {
            cost = 5000;
            next = SectRank.VICE_LEADER;
        } else {
            player.sendMessage(ChatColor.GRAY + "你已是最高职位。");
            return;
        }

        if (!charge(player, cost)) return;

        if (dataManager.setRank(clan.getName(), player.getUniqueId(), next)) {
            player.sendMessage(ChatColor.YELLOW + "职位升级成功！");
        } else {
            player.sendMessage(ChatColor.AQUA + "职位升级失败。");
        }
    }

    private void handleLevelUp(SectClan clan, Player player) {
        if (!clan.getLeaderUuid().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.AQUA + "只有宗主可以升级宗门等级。");
            return;
        }

        int nextLevel = clan.getLevel() + 1;
        double cost = nextLevel * 5000.0;
        if (!charge(player, cost)) return;

        clan.setLevel(nextLevel);
        dataManager.save();
        player.sendMessage(ChatColor.YELLOW + "宗门等级提升至 " + nextLevel + "！");
    }

    private void handleWar(SectClan clan, Player player, ClickType click) {
        if (!clan.getLeaderUuid().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.AQUA + "只有宗主可以发起或接受宗门战。");
            return;
        }

        if (click == ClickType.RIGHT) {
            acceptWar(clan, player);
        } else {
            inviteWar(clan, player);
        }
    }

    private void inviteWar(SectClan clan, Player player) {
        String targetName = getItemName(player.getInventory().getItemInMainHand());
        if (targetName == null) {
            player.sendMessage(ChatColor.GRAY + "请手持写有目标宗门名称的物品，左键点击发起宗门战。");
            return;
        }

        SectClan target = dataManager.getSect(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.AQUA + "未找到宗门 " + targetName + "。");
            return;
        }

        if (target.getName().equals(clan.getName())) {
            player.sendMessage(ChatColor.AQUA + "不能向自己的宗门发起宗门战。");
            return;
        }

        Player targetLeader = Bukkit.getPlayer(target.getLeaderUuid());
        if (targetLeader == null) {
            player.sendMessage(ChatColor.AQUA + "目标宗门宗主不在线。");
            return;
        }

        warRequests.put(target.getName(), clan.getName());
        targetLeader.sendMessage(ChatColor.YELLOW + "[" + clan.getName() + "] 向你的宗门发起宗门战邀请！右键点击管理界面的宗门战按钮接受。");
        player.sendMessage(ChatColor.GRAY + "已向 [" + target.getName() + "] 发起宗门战邀请。");
    }

    private void acceptWar(SectClan clan, Player player) {
        String challengerName = warRequests.get(clan.getName());
        if (challengerName == null) {
            player.sendMessage(ChatColor.AQUA + "当前没有待接受的宗门战邀请。");
            return;
        }

        SectClan challenger = dataManager.getSect(challengerName);
        if (challenger == null) {
            warRequests.remove(clan.getName());
            player.sendMessage(ChatColor.AQUA + "发起宗门战邀约的宗门已不存在。");
            return;
        }

        Player challengerLeader = Bukkit.getPlayer(challenger.getLeaderUuid());
        if (challengerLeader != null) {
            challengerLeader.sendMessage(ChatColor.YELLOW + "[" + clan.getName() + "] 已接受你的宗门战邀请！");
        }

        player.sendMessage(ChatColor.YELLOW + "已接受来自 [" + challengerName + "] 的宗门战邀请。");
        warRequests.remove(clan.getName());
    }

    private String getItemName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (!item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            return ChatColor.stripColor(meta.getDisplayName());
        }
        if (meta instanceof SkullMeta skull && skull.getOwningPlayer() != null) {
            return skull.getOwningPlayer().getName();
        }
        return null;
    }

    private boolean charge(Player player, double amount) {
        Economy economy = getEconomy();
        if (economy == null) {
            player.sendMessage(ChatColor.AQUA + "经济插件未安装，无法执行此操作。");
            return false;
        }

        if (!economy.has(player, amount)) {
            player.sendMessage(ChatColor.AQUA + "金钱不足，需要 " + economy.format(amount) + "。");
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            player.sendMessage(ChatColor.AQUA + "扣款失败：" + response.errorMessage);
            return false;
        }
        return true;
    }

    private Economy getEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return null;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : rsp.getProvider();
    }
}