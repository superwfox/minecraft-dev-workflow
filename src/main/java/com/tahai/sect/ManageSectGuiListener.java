package com.tahai.sect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

public class ManageSectGuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) return;

        if (holder instanceof ManageSectGuiHolder mh) {
            handleManageClick(event, mh);
        } else if (holder instanceof InviteGuiHolder ih) {
            handleInviteClick(event, ih);
        } else if (holder instanceof ConfirmGuiHolder ch) {
            handleConfirmClick(event, ch);
        } else if (holder instanceof PositionGuiHolder ph) {
            handlePositionClick(event, ph);
        } else if (holder instanceof WarGuiHolder wh) {
            handleWarClick(event, wh);
        }
    }

    private void handleManageClick(InventoryClickEvent event, ManageSectGuiHolder holder) {
        event.setCancelled(true);
        Player viewer = holder.getPlayer();
        String guild = holder.getGuildName();
        GuildManager gm = holder.getGuildManager();
        if (viewer == null || guild == null || gm == null) return;

        switch (event.getRawSlot()) {
            case ManageSectGuiHolder.SLOT_INVITE -> {
                InviteGuiHolder ih = new InviteGuiHolder(viewer, guild, gm);
                viewer.openInventory(ih.getInventory());
            }
            case ManageSectGuiHolder.SLOT_UPGRADE -> {
                ConfirmGuiHolder ch = new ConfirmGuiHolder(viewer, "升级确认", () -> {
                    boolean ok = gm.upgradeGuild(viewer, guild);
                    viewer.sendMessage((ok ? ChatColor.YELLOW : ChatColor.AQUA) + (ok ? "升级成功" : "升级失败"));
                });
                viewer.openInventory(ch.getInventory());
            }
            case ManageSectGuiHolder.SLOT_POSITION -> {
                PositionGuiHolder ph = new PositionGuiHolder(viewer, guild, gm);
                viewer.openInventory(ph.getInventory());
            }
            case ManageSectGuiHolder.SLOT_WAR -> {
                WarGuiHolder wh = new WarGuiHolder(viewer, guild, gm);
                viewer.openInventory(wh.getInventory());
            }
            case ManageSectGuiHolder.SLOT_DISBAND -> {
                ConfirmGuiHolder dh = new ConfirmGuiHolder(viewer, "解散确认", () -> {
                    boolean ok = gm.deleteGuild(guild);
                    viewer.sendMessage((ok ? ChatColor.YELLOW : ChatColor.AQUA) + (ok ? "宗门已解散" : "解散失败"));
                });
                viewer.openInventory(dh.getInventory());
            }
            default -> { }
        }
    }

    private void handleInviteClick(InventoryClickEvent event, InviteGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < holder.candidates.size()) {
            Player target = holder.candidates.get(slot);
            if (!target.isOnline()) {
                holder.viewer.sendMessage(ChatColor.AQUA + "该玩家已下线");
                return;
            }
            boolean applied = holder.guildManager.applyJoin(target, holder.guildName);
            if (applied) {
                boolean accepted = holder.guildManager.handleJoin(holder.viewer, holder.guildName, target.getName(), true);
                if (accepted) {
                    holder.viewer.sendMessage(ChatColor.YELLOW + "已将 " + target.getName() + " 加入宗门！");
                    target.sendMessage(ChatColor.YELLOW + "你已被邀请加入宗门 " + holder.guildName);
                } else {
                    holder.viewer.sendMessage(ChatColor.AQUA + "邀请失败，无法接受申请");
                }
            } else {
                holder.viewer.sendMessage(ChatColor.AQUA + "邀请失败，对方可能已有宗门或已申请");
            }
            holder.viewer.closeInventory();
        }
    }

    private void handleConfirmClick(InventoryClickEvent event, ConfirmGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == ConfirmGuiHolder.SLOT_CONFIRM) {
            holder.onConfirm.run();
            holder.viewer.closeInventory();
        } else if (slot == ConfirmGuiHolder.SLOT_CANCEL) {
            holder.viewer.closeInventory();
        }
    }

    private void handlePositionClick(InventoryClickEvent event, PositionGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < holder.targets.size()) {
            Player target = holder.targets.get(slot);
            if (!target.isOnline()) {
                holder.viewer.sendMessage(ChatColor.AQUA + "该玩家已下线");
                return;
            }
            boolean ok = holder.guildManager.promoteMember(holder.viewer, holder.guildName, target);
            holder.viewer.sendMessage((ok ? ChatColor.YELLOW : ChatColor.AQUA) + (ok ? "已调整职位" : "无法调整职位"));
            holder.viewer.closeInventory();
        }
    }

    private void handleWarClick(InventoryClickEvent event, WarGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < holder.guilds.size()) {
            String target = holder.guilds.get(slot);
            boolean ok = holder.guildManager.startWar(holder.guildName, target);
            holder.viewer.sendMessage((ok ? ChatColor.YELLOW : ChatColor.AQUA) + (ok ? "宣战成功" : "宣战失败"));
            holder.viewer.closeInventory();
        }
    }

    private static class InviteGuiHolder implements InventoryHolder {
        final Inventory inv;
        final Player viewer;
        final String guildName;
        final GuildManager guildManager;
        final List<Player> candidates;

        InviteGuiHolder(Player viewer, String guildName, GuildManager gm) {
            this.viewer = viewer;
            this.guildName = guildName;
            this.guildManager = gm;
            this.candidates = new ArrayList<>();
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(viewer)) continue;
                if (!isInGuild(plugin, p.getName())) candidates.add(p);
            }
            this.inv = Bukkit.createInventory(this, 54, "邀请在线玩家 - " + guildName);
            for (int i = 0; i < candidates.size() && i < 45; i++) {
                ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof SkullMeta skullMeta) {
                    skullMeta.setOwningPlayer(candidates.get(i));
                    skullMeta.setDisplayName(candidates.get(i).getName());
                    item.setItemMeta(skullMeta);
                } else {
                    item = new ItemStack(Material.PAPER);
                    ItemMeta pm = item.getItemMeta();
                    pm.setDisplayName(candidates.get(i).getName());
                    item.setItemMeta(pm);
                }
                inv.setItem(i, item);
            }
        }

        private boolean isInGuild(Plugin plugin, String playerName) {
            if (plugin == null) return false;
            ConfigurationSection guilds = plugin.getConfig().getConfigurationSection("guilds");
            if (guilds == null) return false;
            for (String guild : guilds.getKeys(false)) {
                if (playerName.equals(guilds.getString(guild + ".leader"))) return true;
                ConfigurationSection members = guilds.getConfigurationSection(guild + ".members");
                if (members != null && members.getKeys(false).contains(playerName)) return true;
            }
            return false;
        }

        @Override
        public Inventory getInventory() { return inv; }
    }

    private static class ConfirmGuiHolder implements InventoryHolder {
        static final int SLOT_CONFIRM = 11;
        static final int SLOT_CANCEL = 15;

        final Inventory inv;
        final Player viewer;
        final Runnable onConfirm;

        ConfirmGuiHolder(Player viewer, String title, Runnable onConfirm) {
            this.viewer = viewer;
            this.onConfirm = onConfirm;
            this.inv = Bukkit.createInventory(this, 27, title);
            ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
            ItemMeta meta = confirm.getItemMeta();
            meta.setDisplayName("确认");
            confirm.setItemMeta(meta);
            inv.setItem(SLOT_CONFIRM, confirm);

            ItemStack cancel = new ItemStack(Material.RED_WOOL);
            ItemMeta cmeta = cancel.getItemMeta();
            cmeta.setDisplayName("取消");
            cancel.setItemMeta(cmeta);
            inv.setItem(SLOT_CANCEL, cancel);
        }

        @Override
        public Inventory getInventory() { return inv; }
    }

    private static class PositionGuiHolder implements InventoryHolder {
        final Inventory inv;
        final Player viewer;
        final String guildName;
        final GuildManager guildManager;
        final List<Player> targets;

        PositionGuiHolder(Player viewer, String guildName, GuildManager gm) {
            this.viewer = viewer;
            this.guildName = guildName;
            this.guildManager = gm;
            this.targets = new ArrayList<>();
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
            Set<String> names = new HashSet<>();
            if (plugin != null) {
                ConfigurationSection guildSec = plugin.getConfig().getConfigurationSection("guilds." + guildName);
                if (guildSec != null) {
                    String leader = guildSec.getString("leader");
                    if (leader != null) names.add(leader);
                    ConfigurationSection members = guildSec.getConfigurationSection("members");
                    if (members != null) names.addAll(members.getKeys(false));
                }
            }
            for (String name : names) {
                Player p = Bukkit.getPlayerExact(name);
                if (p != null && !p.getName().equals(viewer.getName())) targets.add(p);
            }
            this.inv = Bukkit.createInventory(this, 54, "调整职位 - " + guildName);
            for (int i = 0; i < targets.size() && i < 45; i++) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(targets.get(i).getName());
                item.setItemMeta(meta);
                inv.setItem(i, item);
            }
        }

        @Override
        public Inventory getInventory() { return inv; }
    }

    private static class WarGuiHolder implements InventoryHolder {
        final Inventory inv;
        final Player viewer;
        final String guildName;
        final GuildManager guildManager;
        final List<String> guilds;

        WarGuiHolder(Player viewer, String guildName, GuildManager gm) {
            this.viewer = viewer;
            this.guildName = guildName;
            this.guildManager = gm;
            this.guilds = new ArrayList<>();
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
            if (plugin != null) {
                ConfigurationSection section = plugin.getConfig().getConfigurationSection("guilds");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        if (!key.equals(guildName)) guilds.add(key);
                    }
                }
            }
            this.inv = Bukkit.createInventory(this, 54, "选择宣战对象");
            for (int i = 0; i < guilds.size() && i < 45; i++) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(guilds.get(i));
                item.setItemMeta(meta);
                inv.setItem(i, item);
            }
        }

        @Override
        public Inventory getInventory() { return inv; }
    }
}