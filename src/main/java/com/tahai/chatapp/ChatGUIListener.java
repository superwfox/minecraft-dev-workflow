package com.tahai.chatapp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatGUIListener implements Listener {

    private final Plugin plugin;
    private final Set<UUID> switching = new HashSet<>();
    private final NamespacedKey actionKey;
    private final NamespacedKey channelKey;
    private final NamespacedKey targetKey;
    private final NamespacedKey groupIdKey;
    private final NamespacedKey valueKey;
    private DataManager dataManager;

    public ChatGUIListener() {
        this.plugin = Bukkit.getPluginManager().getPlugin("ChatApp");
        if (this.plugin == null) {
            throw new IllegalStateException("ChatApp plugin not found");
        }
        this.actionKey = new NamespacedKey(plugin, "gui_action");
        this.channelKey = new NamespacedKey(plugin, "gui_channel");
        this.targetKey = new NamespacedKey(plugin, "gui_target");
        this.groupIdKey = new NamespacedKey(plugin, "gui_group");
        this.valueKey = new NamespacedKey(plugin, "gui_value");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof ChatGUIHolder holder)) return;

        event.setCancelled(true);

        if (event.getRawSlot() < 0 || event.getRawSlot() >= inv.getSize()) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        getDataManager();
        holder.setDataManager(dataManager);

        if (event.getView().getType() == InventoryType.ANVIL && event.getRawSlot() == 2) {
            handleAnvilConfirm(player, holder, item, holder.getInventory().getItem(0));
            return;
        }

        String action = getString(item, actionKey);
        if (action == null) return;
        handleAction(player, holder, item, action);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (switching.remove(player.getUniqueId())) return;
        if (!(event.getInventory().getHolder() instanceof ChatGUIHolder holder)) return;

        holder.setSessionChannel(player, null);
        holder.setSessionPrivateTarget(player, null);
        holder.setSessionGroup(player, 0);
        holder.setSessionDraft(player, null);
    }

    private void handleAction(Player p, ChatGUIHolder holder, ItemStack item, String action) {
        switch (action) {
            case "open_public", "main_public" -> {
                holder.setSessionChannel(p, "public");
                holder.setSessionPrivateTarget(p, null);
                holder.setSessionGroup(p, 0);
                openGui(p, () -> holder.openChannel(p, "public"));
            }
            case "open_private", "main_private" -> {
                holder.setSessionChannel(p, "private");
                UUID target = holder.getSessionPrivateTarget(p);
                if (target != null) {
                    openGui(p, () -> holder.openChannel(p, "private"));
                } else {
                    openGui(p, () -> holder.openFriendManager(p));
                }
            }
            case "open_group", "main_group" -> {
                holder.setSessionChannel(p, "group");
                int groupId = holder.getSessionGroup(p);
                if (groupId > 0 && dataManager.getGroup(groupId) != null) {
                    openGui(p, () -> holder.openChannel(p, "group"));
                } else {
                    openGui(p, () -> holder.openGroupManager(p));
                }
            }
            case "open_friends", "main_friends", "channel_friend" ->
                    openGui(p, () -> holder.openFriendManager(p));
            case "open_groups", "main_groups", "channel_group" ->
                    openGui(p, () -> holder.openGroupManager(p));
            case "open_channel" -> {
                String channel = getString(item, channelKey);
                if (channel != null) {
                    holder.setSessionChannel(p, channel);
                    openGui(p, () -> holder.openChannel(p, channel));
                }
            }
            case "back" -> openGui(p, () -> holder.openMainMenu(p));
            case "input_text", "channel_input" -> {
                holder.setSessionDraft(p, "send_text");
                openGui(p, () -> holder.openAnvilInput(p, false));
            }
            case "input_command", "channel_command" -> {
                holder.setSessionDraft(p, "send_command");
                openGui(p, () -> holder.openAnvilInput(p, true));
            }
            case "open_emoji", "channel_emoji" ->
                    openGui(p, () -> holder.openEmoji(p));
            case "emoji" -> {
                String emoji = getString(item, valueKey);
                if (emoji == null && item.hasItemMeta()) emoji = item.getItemMeta().getDisplayName();
                if (emoji != null) sendChannelMessage(p, holder, emoji);
            }
            case "add_friend", "friend_add" -> {
                UUID target = getUuid(item, targetKey);
                if (target != null) sendFriendRequest(p, holder, target);
            }
            case "add_friend_offline", "friend_add_offline" -> {
                holder.setSessionDraft(p, "add_friend_offline");
                openGui(p, () -> holder.openAnvilInput(p, false));
            }
            case "accept_friend", "friend_accept" -> {
                UUID requester = getUuid(item, targetKey);
                if (requester != null && dataManager.acceptFriendRequest(requester, p.getUniqueId())) {
                    p.sendMessage(ChatColor.YELLOW + "已同意好友请求");
                } else {
                    p.sendMessage(ChatColor.AQUA + "好友请求不存在或已失效");
                }
                openGui(p, () -> holder.openFriendManager(p));
            }
            case "decline_friend", "friend_decline" -> {
                UUID requester = getUuid(item, targetKey);
                if (requester != null) dataManager.declineFriendRequest(requester, p.getUniqueId());
                openGui(p, () -> holder.openFriendManager(p));
            }
            case "set_private", "friend_private" -> {
                UUID target = getUuid(item, targetKey);
                if (target != null && !target.equals(p.getUniqueId())) {
                    holder.setSessionPrivateTarget(p, target);
                    holder.setSessionChannel(p, "private");
                    openGui(p, () -> holder.openChannel(p, "private"));
                }
            }
            case "create_group", "group_create" -> {
                holder.setSessionDraft(p, "create_group");
                openGui(p, () -> holder.openAnvilInput(p, false));
            }
            case "select_group" -> {
                Integer groupId = getInt(item, groupIdKey);
                if (groupId != null) {
                    holder.setSessionGroup(p, groupId);
                    holder.setSessionChannel(p, "group");
                    openGui(p, () -> holder.openChannel(p, "group"));
                }
            }
            case "join_group", "group_join" -> {
                Integer groupId = getInt(item, groupIdKey);
                if (groupId != null) joinGroup(p, holder, groupId);
            }
            case "toggle_review", "group_review" -> {
                Integer groupId = getInt(item, groupIdKey);
                if (groupId != null) toggleReview(p, holder, groupId);
            }
            case "kick_member", "group_kick" -> {
                Integer groupId = getInt(item, groupIdKey);
                UUID target = getUuid(item, targetKey);
                if (groupId != null && target != null) kickMember(p, holder, groupId, target);
            }
            case "mute_member", "group_mute" -> {
                Integer groupId = getInt(item, groupIdKey);
                UUID target = getUuid(item, targetKey);
                if (groupId != null && target != null) setMute(p, holder, groupId, target, true);
            }
            case "unmute_member", "group_unmute" -> {
                Integer groupId = getInt(item, groupIdKey);
                UUID target = getUuid(item, targetKey);
                if (groupId != null && target != null) setMute(p, holder, groupId, target, false);
            }
            case "set_admin", "group_admin" -> {
                Integer groupId = getInt(item, groupIdKey);
                UUID target = getUuid(item, targetKey);
                if (groupId != null && target != null) setAdmin(p, holder, groupId, target, true);
            }
            case "remove_admin", "group_unadmin" -> {
                Integer groupId = getInt(item, groupIdKey);
                UUID target = getUuid(item, targetKey);
                if (groupId != null && target != null) setAdmin(p, holder, groupId, target, false);
            }
            case "transfer_owner", "group_transfer" -> {
                Integer groupId = getInt(item, groupIdKey);
                UUID target = getUuid(item, targetKey);
                if (groupId != null && target != null) transferOwner(p, holder, groupId, target);
            }
            case "delete_group", "group_delete" -> {
                Integer groupId = getInt(item, groupIdKey);
                if (groupId != null) deleteGroup(p, holder, groupId);
            }
            default -> {
                if (action.startsWith("open_channel:")) {
                    String channel = action.substring("open_channel:".length());
                    holder.setSessionChannel(p, channel);
                    openGui(p, () -> holder.openChannel(p, channel));
                }
            }
        }
    }

    private void handleAnvilConfirm(Player p, ChatGUIHolder holder, ItemStack result, ItemStack input) {
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;
        String text = meta.getDisplayName();
        if (text == null || text.trim().isEmpty()) return;

        String action = getString(result, actionKey);
        if (action == null && input != null) action = getString(input, actionKey);
        String draft = holder.getSessionDraft(p);
        if (draft == null) draft = holder.getDraft();
        if (action == null) action = draft;
        if (action == null) action = "send_text";

        switch (action) {
            case "send_command" -> {
                holder.setSessionDraft(p, null);
                String command = text.startsWith("/") ? text.substring(1) : text;
                Bukkit.dispatchCommand(p, command);
                String channel = holder.getSessionChannel(p);
                String finalChannel = channel == null ? "public" : channel;
                openGui(p, () -> holder.openChannel(p, finalChannel));
            }
            case "create_group" -> {
                holder.setSessionDraft(p, null);
                createGroupByName(p, holder, text);
            }
            case "add_friend_offline" -> {
                holder.setSessionDraft(p, null);
                addFriendByName(p, holder, text);
            }
            default -> {
                holder.setSessionDraft(p, null);
                sendChannelMessage(p, holder, text);
            }
        }
    }

    private void sendChannelMessage(Player p, ChatGUIHolder holder, String text) {
        String channel = holder.getSessionChannel(p);
        if (channel == null) channel = holder.getChannelKey();
        if (channel == null) {
            p.sendMessage(ChatColor.AQUA + "请先选择频道");
            return;
        }

        switch (channel) {
            case "public" -> {
                dataManager.addPublicMessage(p.getUniqueId(), text);
                Bukkit.broadcastMessage(p.getName() + ": " + text);
                openGui(p, () -> holder.openChannel(p, "public"));
            }
            case "private" -> {
                UUID target = holder.getSessionPrivateTarget(p);
                if (target == null) target = holder.getPrivateTarget();
                if (target == null) {
                    p.sendMessage(ChatColor.AQUA + "请先选择私聊对象");
                    openGui(p, () -> holder.openFriendManager(p));
                    return;
                }
                dataManager.addPrivateMessage(p.getUniqueId(), target, p.getUniqueId(), text);
                String message = p.getName() + " -> " + playerName(target) + ": " + text;
                p.sendMessage(message);
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    targetPlayer.sendMessage(message);
                }
                openGui(p, () -> holder.openChannel(p, "private"));
            }
            case "group" -> {
                int groupId = holder.getSessionGroup(p);
                if (groupId <= 0) groupId = holder.getGroupId();
                if (groupId <= 0) {
                    p.sendMessage(ChatColor.AQUA + "请先选择群");
                    openGui(p, () -> holder.openGroupManager(p));
                    return;
                }
                if (!dataManager.isGroupMember(groupId, p.getUniqueId())) {
                    p.sendMessage(ChatColor.AQUA + "你不是该群成员");
                    return;
                }
                if (dataManager.isGroupMuted(groupId, p.getUniqueId())) {
                    p.sendMessage(ChatColor.AQUA + "你已被禁言");
                    return;
                }
                dataManager.addGroupMessage(groupId, p.getUniqueId(), text);
                GroupData group = dataManager.getGroup(groupId);
                if (group != null) {
                    String message = "[" + group.getName() + "] " + p.getName() + ": " + text;
                    for (UUID member : group.getMembers()) {
                        Player memberPlayer = Bukkit.getPlayer(member);
                        if (memberPlayer != null && memberPlayer.isOnline()) {
                            memberPlayer.sendMessage(message);
                        }
                    }
                }
                openGui(p, () -> holder.openChannel(p, "group"));
            }
            default -> {
                dataManager.addPublicMessage(p.getUniqueId(), text);
                Bukkit.broadcastMessage(p.getName() + ": " + text);
                openGui(p, () -> holder.openChannel(p, channel));
            }
        }
    }

    private void sendFriendRequest(Player p, ChatGUIHolder holder, UUID target) {
        if (target.equals(p.getUniqueId())) return;
        if (dataManager.areFriends(p.getUniqueId(), target)) {
            p.sendMessage(ChatColor.AQUA + "你们已经是好友了");
        } else if (dataManager.sendFriendRequest(p.getUniqueId(), target)) {
            p.sendMessage(ChatColor.YELLOW + "好友请求已发送");
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(ChatColor.YELLOW + p.getName() + " 向你发送了好友请求");
            }
        } else {
            p.sendMessage(ChatColor.AQUA + "好友请求发送失败，可能已存在待处理请求");
        }
        openGui(p, () -> holder.openFriendManager(p));
    }

    private void addFriendByName(Player p, ChatGUIHolder holder, String name) {
        Player online = Bukkit.getPlayerExact(name);
        UUID target;
        if (online != null) {
            target = online.getUniqueId();
        } else {
            target = Bukkit.getOfflinePlayer(name).getUniqueId();
        }
        sendFriendRequest(p, holder, target);
    }

    private void createGroupByName(Player p, ChatGUIHolder holder, String name) {
        int groupId = dataManager.createGroup(name, p.getUniqueId());
        if (groupId <= 0) {
            p.sendMessage(ChatColor.AQUA + "群创建失败");
            openGui(p, () -> holder.openGroupManager(p));
            return;
        }
        if (!dataManager.isGroupMember(groupId, p.getUniqueId())) {
            dataManager.addGroupMember(groupId, p.getUniqueId());
        }
        p.sendMessage(ChatColor.YELLOW + "群 " + name + " 创建成功");
        holder.setSessionGroup(p, groupId);
        holder.setSessionChannel(p, "group");
        openGui(p, () -> holder.openChannel(p, "group"));
    }

    private void joinGroup(Player p, ChatGUIHolder holder, int groupId) {
        GroupData group = dataManager.getGroup(groupId);
        if (group == null) {
            p.sendMessage(ChatColor.AQUA + "群不存在");
            return;
        }
        if (group.getMembers().contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + "你已在群中");
            return;
        }
        if (dataManager.isGroupReviewEnabled(groupId)) {
            p.sendMessage(ChatColor.YELLOW + "已提交入群申请，等待群主审核");
        } else if (dataManager.addGroupMember(groupId, p.getUniqueId())) {
            p.sendMessage(ChatColor.YELLOW + "已加入群 " + group.getName());
            holder.setSessionGroup(p, groupId);
            holder.setSessionChannel(p, "group");
            openGui(p, () -> holder.openChannel(p, "group"));
            return;
        }
        openGui(p, () -> holder.openGroupManager(p));
    }

    private void toggleReview(Player p, ChatGUIHolder holder, int groupId) {
        GroupData group = dataManager.getGroup(groupId);
        if (group == null) return;
        if (!group.getOwner().equals(p.getUniqueId()) && !dataManager.isGroupAdmin(groupId, p.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + "没有权限");
            return;
        }
        boolean enabled = !dataManager.isGroupReviewEnabled(groupId);
        dataManager.setGroupReviewEnabled(groupId, enabled);
        p.sendMessage(ChatColor.YELLOW + "入群审核已" + (enabled ? "开启" : "关闭"));
        openGui(p, () -> holder.openGroupManager(p));
    }

    private void kickMember(Player p, ChatGUIHolder holder, int groupId, UUID target) {
        GroupData group = dataManager.getGroup(groupId);
        if (group == null) return;
        if (!group.getOwner().equals(p.getUniqueId()) && !dataManager.isGroupAdmin(groupId, p.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + "没有权限");
            return;
        }
        if (group.getOwner().equals(target)) {
            p.sendMessage(ChatColor.AQUA + "不能移出群主");
            return;
        }
        dataManager.removeGroupMember(groupId, target);
        p.sendMessage(ChatColor.YELLOW + "已移出群成员");
        openGui(p, () -> holder.openGroupManager(p));
    }

    private void setMute(Player p, ChatGUIHolder holder, int groupId, UUID target, boolean muted) {
        GroupData group = dataManager.getGroup(groupId);
        if (group == null) return;
        if (!group.getOwner().equals(p.getUniqueId()) && !dataManager.isGroupAdmin(groupId, p.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + "没有权限");
            return;
        }
        dataManager.setGroupMuted(groupId, target, muted);
        p.sendMessage(ChatColor.YELLOW + (muted ? "已禁言" : "已解除禁言"));
        openGui(p, () -> holder.openGroupManager(p));
    }

    private void setAdmin(Player p, ChatGUIHolder holder, int groupId, UUID target, boolean admin) {
        GroupData group = dataManager.getGroup(groupId);
        if (group == null) return;
        if (!group.getOwner().equals(p.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + "只有群主可以设置管理员");
            return;
        }
        if (admin) {
            dataManager.addGroupAdmin(groupId, target);
            p.sendMessage(ChatColor.YELLOW + "已设为管理员");
        } else {
            dataManager.removeGroupAdmin(groupId, target);
            p.sendMessage(ChatColor.YELLOW + "已取消管理员");
        }
        openGui(p, () -> holder.openGroupManager(p));
    }

    private void transferOwner(Player p, ChatGUIHolder holder, int groupId, UUID target) {
        GroupData old = dataManager.getGroup(groupId);
        if (old == null) return;
        if (!old.getOwner().equals(p.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + "只有群主可以转让群");
            return;
        }
        if (target.equals(p.getUniqueId())) return;

        int newId = dataManager.createGroup(old.getName(), target);
        if (newId <= 0) {
            p.sendMessage(ChatColor.AQUA + "转让失败");
            return;
        }
        dataManager.addGroupMember(newId, target);
        for (UUID member : old.getMembers()) {
            dataManager.addGroupMember(newId, member);
        }
        for (UUID admin : old.getAdmins()) {
            dataManager.addGroupAdmin(newId, admin);
        }
        for (UUID muted : old.getMuted()) {
            dataManager.setGroupMuted(newId, muted, true);
        }
        dataManager.setGroupReviewEnabled(newId, old.isReviewEnabled());
        dataManager.deleteGroup(groupId);

        p.sendMessage(ChatColor.YELLOW + "群主已转让给 " + playerName(target));
        holder.setSessionGroup(p, newId);
        holder.setSessionChannel(p, "group");
        openGui(p, () -> holder.openGroupManager(p));
    }

    private void deleteGroup(Player p, ChatGUIHolder holder, int groupId) {
        GroupData group = dataManager.getGroup(groupId);
        if (group == null) return;
        if (!group.getOwner().equals(p.getUniqueId())) {
            p.sendMessage(ChatColor.AQUA + "只有群主可以解散群");
            return;
        }
        dataManager.deleteGroup(groupId);
        p.sendMessage(ChatColor.YELLOW + "群已解散");
        holder.setSessionGroup(p, 0);
        holder.setSessionChannel(p, "public");
        openGui(p, () -> holder.openMainMenu(p));
    }

    private void openGui(Player p, Runnable openAction) {
        switching.add(p.getUniqueId());
        openAction.run();
        Bukkit.getScheduler().runTask(plugin, () -> switching.remove(p.getUniqueId()));
    }

    private DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = new DataManager(plugin);
        }
        return dataManager;
    }

    private String playerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        return offline.getName() == null ? uuid.toString() : offline.getName();
    }

    private String getString(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private Integer getInt(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }

    private UUID getUuid(ItemStack item, NamespacedKey key) {
        String value = getString(item, key);
        if (value == null || value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}