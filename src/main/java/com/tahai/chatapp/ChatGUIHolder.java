package com.tahai.chatapp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

public class ChatGUIHolder implements InventoryHolder {

    private static final Map<Player, Session> sessions = new HashMap<>();
    private static DataManager dataManager;

    private final Player player;
    private Inventory inventory;
    private GuiType type;
    private String channelKey;
    private int groupId = -1;
    private UUID privateTarget;
    private String draft = "";

    private ChatGUIHolder(Player player) {
        this.player = player;
    }

    public static void setDataManager(DataManager dm) {
        dataManager = dm;
    }

    private static DataManager getDataManager() {
        if (dataManager == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("ChatApp");
            dataManager = new DataManager(plugin);
        }
        return dataManager;
    }

    public static Session getSession(Player p) {
        return sessions.computeIfAbsent(p, k -> new Session());
    }

    public static String getSessionChannel(Player p) {
        return getSession(p).channelKey;
    }

    public static UUID getSessionPrivateTarget(Player p) {
        return getSession(p).privateTarget;
    }

    public static int getSessionGroup(Player p) {
        return getSession(p).groupId;
    }

    public static String getSessionDraft(Player p) {
        return getSession(p).draft;
    }

    public static void setSessionChannel(Player p, String key) {
        getSession(p).channelKey = key;
    }

    public static void setSessionPrivateTarget(Player p, UUID target) {
        getSession(p).privateTarget = target;
    }

    public static void setSessionGroup(Player p, int groupId) {
        getSession(p).groupId = groupId;
    }

    public static void setSessionDraft(Player p, String draft) {
        getSession(p).draft = draft;
    }

    public GuiType getType() {
        return type;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getDraft() {
        return draft;
    }

    public UUID getPrivateTarget() {
        return privateTarget;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static ChatGUIHolder openMainMenu(Player p) {
        ChatGUIHolder h = new ChatGUIHolder(p);
        h.type = GuiType.MAIN;
        h.inventory = Bukkit.createInventory(h, 27, ChatColor.GRAY + "聊天系统");
        h.inventory.setItem(0, item(Material.PAPER, ChatColor.YELLOW + "公共频道", "点击进入公共频道"));
        h.inventory.setItem(1, item(Material.BOOK, ChatColor.YELLOW + "个人频道", "点击进入私聊"));
        h.inventory.setItem(2, item(Material.ENDER_CHEST, ChatColor.YELLOW + "群频道", "点击进入群聊"));
        h.inventory.setItem(9, item(Material.PLAYER_HEAD, ChatColor.YELLOW + "好友管理", "管理好友"));
        h.inventory.setItem(10, item(Material.NOTE_BLOCK, ChatColor.YELLOW + "群管理", "管理群组"));
        p.openInventory(h.inventory);
        return h;
    }

    public static ChatGUIHolder openChannel(Player p, String channelKey) {
        ChatGUIHolder h = new ChatGUIHolder(p);
        h.type = GuiType.CHANNEL;
        h.channelKey = channelKey;
        getSession(p).channelKey = channelKey;
        h.inventory = Bukkit.createInventory(h, 54, ChatColor.GRAY + "频道 - " + channelKey);

        var messages = getDataManager().getChannelMessages(channelKey);
        int idx = 0;
        for (int i = Math.max(0, messages.size() - 36); i < messages.size() && idx < 36; i++) {
            var msg = messages.get(i);
            long ts = msg.getTimestamp();
            String time = String.format("%tH:%tM", ts, ts);
            String senderName = Bukkit.getOfflinePlayer(msg.getSender()).getName();
            if (senderName == null) senderName = msg.getSender().toString().substring(0, 8);
            h.inventory.setItem(idx, item(Material.BOOK, ChatColor.WHITE + senderName,
                    ChatColor.GRAY + time, ChatColor.GRAY + msg.getContent()));
            idx++;
        }

        int base = 45;
        h.inventory.setItem(base, item(Material.WRITABLE_BOOK, ChatColor.YELLOW + "输入文字", "点击输入"));
        h.inventory.setItem(base + 1, item(Material.EMERALD, ChatColor.YELLOW + "表情", "选择表情"));
        h.inventory.setItem(base + 2, item(Material.COMMAND_BLOCK, ChatColor.YELLOW + "命令输入", "输入命令"));
        h.inventory.setItem(base + 3, item(Material.PLAYER_HEAD, ChatColor.YELLOW + "好友管理", "管理好友"));
        h.inventory.setItem(base + 4, item(Material.NOTE_BLOCK, ChatColor.YELLOW + "群管理", "管理群组"));
        p.openInventory(h.inventory);
        return h;
    }

    public static ChatGUIHolder openAnvilInput(Player p, boolean commandMode) {
        ChatGUIHolder h = new ChatGUIHolder(p);
        h.type = GuiType.ANVIL_INPUT;
        h.draft = commandMode ? "/" : "";
        h.inventory = Bukkit.createInventory(h, 9, ChatColor.GRAY + "Anvil 输入");
        h.inventory.setItem(0, item(Material.PAPER, ChatColor.YELLOW + (commandMode ? "命令输入" : "文字输入"),
                ChatColor.GRAY + "请点击后输入内容"));
        h.inventory.setItem(4, item(Material.ANVIL, ChatColor.YELLOW + "确认", "点击发送"));
        h.inventory.setItem(8, item(Material.BARRIER, ChatColor.YELLOW + "返回", "返回频道"));
        p.openInventory(h.inventory);
        return h;
    }

    public static ChatGUIHolder openEmoji(Player p) {
        ChatGUIHolder h = new ChatGUIHolder(p);
        h.type = GuiType.EMOJI;
        h.inventory = Bukkit.createInventory(h, 36, ChatColor.GRAY + "表情选择");
        String[] emojis = {":)", ":(", ":D", ";)", "<3", ":P", "XD", "-_-", ":O", ":'(", ">_<", "=)"};
        for (int i = 0; i < emojis.length && i < 36; i++) {
            h.inventory.setItem(i, item(Material.PAPER, ChatColor.YELLOW + emojis[i]));
        }
        h.inventory.setItem(35, item(Material.BARRIER, ChatColor.YELLOW + "返回"));
        p.openInventory(h.inventory);
        return h;
    }

    public static ChatGUIHolder openFriendManager(Player p) {
        ChatGUIHolder h = new ChatGUIHolder(p);
        h.type = GuiType.FRIEND;
        h.inventory = Bukkit.createInventory(h, 54, ChatColor.GRAY + "好友管理");
        int idx = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(p) && idx < 36) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(online);
                meta.setDisplayName(ChatColor.YELLOW + online.getName());
                meta.setLore(Arrays.asList(ChatColor.GRAY + "点击设置私聊目标"));
                head.setItemMeta(meta);
                h.inventory.setItem(idx, head);
                idx++;
            }
        }
        h.inventory.setItem(45, item(Material.WRITABLE_BOOK, ChatColor.YELLOW + "添加好友", "输入玩家名"));
        h.inventory.setItem(46, item(Material.BOOK, ChatColor.YELLOW + "待处理请求", "查看请求"));
        h.inventory.setItem(53, item(Material.BARRIER, ChatColor.YELLOW + "返回"));
        p.openInventory(h.inventory);
        return h;
    }

    public static ChatGUIHolder openGroupManager(Player p) {
        ChatGUIHolder h = new ChatGUIHolder(p);
        h.type = GuiType.GROUP;
        h.inventory = Bukkit.createInventory(h, 54, ChatColor.GRAY + "群管理");
        DataManager dm = getDataManager();
        int idx = 0;
        for (var group : dm.getAllGroups()) {
            if (group.getMembers().contains(p.getUniqueId()) && idx < 27) {
                h.inventory.setItem(idx, item(Material.ENDER_CHEST, ChatColor.YELLOW + group.getName(),
                        "ID: " + group.getId(), "成员: " + group.getMembers().size()));
                idx++;
            }
        }
        h.inventory.setItem(27, item(Material.PAPER, ChatColor.YELLOW + "创建群", "输入群名"));
        h.inventory.setItem(28, item(Material.BOOK, ChatColor.YELLOW + "申请加入", "输入群ID"));
        h.inventory.setItem(29, item(Material.LEVER, ChatColor.YELLOW + "审核开关", "切换群审核"));
        h.inventory.setItem(30, item(Material.IRON_BOOTS, ChatColor.YELLOW + "踢出", "踢出成员"));
        h.inventory.setItem(31, item(Material.BARRIER, ChatColor.YELLOW + "禁言", "禁言成员"));
        h.inventory.setItem(32, item(Material.STICK, ChatColor.YELLOW + "设置管理员", "设置为管理员"));
        h.inventory.setItem(33, item(Material.DIAMOND, ChatColor.YELLOW + "转让", "转让群主"));
        h.inventory.setItem(34, item(Material.LAVA_BUCKET, ChatColor.YELLOW + "解散", "解散群"));
        h.inventory.setItem(53, item(Material.BARRIER, ChatColor.YELLOW + "返回"));
        p.openInventory(h.inventory);
        return h;
    }

    private static ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    public enum GuiType {
        MAIN,
        CHANNEL,
        ANVIL_INPUT,
        EMOJI,
        FRIEND,
        GROUP
    }

    static class Session {
        String channelKey = "public";
        UUID privateTarget;
        int groupId = -1;
        String draft = "";
    }
}