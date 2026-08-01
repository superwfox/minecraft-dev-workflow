package com.tahai.sect;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SectGUI implements Listener, InventoryHolder {

    public enum GuiType {
        MAIN, JOIN_LIST, CONFIRM_DELETE, WAR_INVITE
    }

    private final Inventory inventory;
    private final SectDataManager dataManager;
    private final GuiType type;
    private int page;
    private String targetSect;
    private String attackerSect;
    private String defenderSect;

    private SectGUI(SectDataManager dataManager, GuiType type, String title, int size) {
        this.dataManager = dataManager;
        this.type = type;
        this.inventory = org.bukkit.Bukkit.createInventory(this, size, title);
    }

    public static SectGUI openMainMenu(Player player, SectDataManager dataManager) {
        SectGUI gui = new SectGUI(dataManager, GuiType.MAIN, "宗门管理", 27);
        gui.buildMainMenu(player);
        gui.open(player);
        return gui;
    }

    public static SectGUI openJoinList(Player player, SectDataManager dataManager, int page) {
        int p = Math.max(1, page);
        SectGUI gui = new SectGUI(dataManager, GuiType.JOIN_LIST, "加入宗门 - 第 " + p + " 页", 36);
        gui.page = p;
        gui.buildJoinList(p);
        gui.open(player);
        return gui;
    }

    public static SectGUI openConfirmDelete(Player player, SectDataManager dataManager, String sectName) {
        SectGUI gui = new SectGUI(dataManager, GuiType.CONFIRM_DELETE, "确认删除宗门", 27);
        gui.targetSect = sectName;
        gui.buildConfirmDelete(sectName);
        gui.open(player);
        return gui;
    }

    public static SectGUI openWarInvite(Player player, SectDataManager dataManager, String attackerSect, String defenderSect) {
        SectGUI gui = new SectGUI(dataManager, GuiType.WAR_INVITE, "宗门战邀请", 27);
        gui.attackerSect = attackerSect;
        gui.defenderSect = defenderSect;
        gui.buildWarInvite(attackerSect, defenderSect);
        gui.open(player);
        return gui;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public SectDataManager getDataManager() {
        return dataManager;
    }

    public GuiType getType() {
        return type;
    }

    public int getPage() {
        return page;
    }

    public String getTargetSect() {
        return targetSect;
    }

    public String getAttackerSect() {
        return attackerSect;
    }

    public String getDefenderSect() {
        return defenderSect;
    }

    private void buildMainMenu(Player player) {
        Sect sect = findPlayerSect(player.getUniqueId());
        boolean isOwner = sect != null && sect.getOwner().equals(player.getUniqueId());
        boolean isMember = sect != null && !isOwner;

        setItem(0, createItem(Material.MAP,
                ChatColor.YELLOW + "宗门信息",
                ChatColor.GRAY + (sect == null ? "你尚未加入任何宗门" : "宗门: " + sect.getName())));
        setItem(1, createItem(Material.EMERALD,
                ChatColor.YELLOW + "加入宗门",
                ChatColor.GRAY + "浏览并加入一个宗门"));

        if (sect != null) {
            setItem(2, createItem(Material.BOOK,
                    ChatColor.YELLOW + "查看成员",
                    ChatColor.GRAY + "查看宗门成员列表"));

            if (isOwner) {
                setItem(3, createItem(Material.NETHER_STAR,
                        ChatColor.YELLOW + "升级宗门",
                        ChatColor.GRAY + "提升宗门等级"));
                setItem(4, createItem(Material.BARRIER,
                        ChatColor.YELLOW + "解散宗门",
                        ChatColor.GRAY + "永久解散当前宗门"));
                setItem(5, createItem(Material.DIAMOND_SWORD,
                        ChatColor.YELLOW + "宗门战",
                        ChatColor.GRAY + "向其他宗门发起战争"));
            } else if (isMember) {
                setItem(6, createItem(Material.RED_BED,
                        ChatColor.YELLOW + "退出宗门",
                        ChatColor.GRAY + "离开当前宗门"));
            }
        }

        fillEmptySlots();
    }

    private void buildJoinList(int currentPage) {
        List<String> sectNames = new ArrayList<>(dataManager.getSects().keySet());
        int start = (currentPage - 1) * 27;
        int end = Math.min(start + 27, sectNames.size());

        for (int i = start, slot = 0; i < end; i++, slot++) {
            String name = sectNames.get(i);
            Sect sect = dataManager.getSect(name);
            String ownerName = sect != null ? getName(sect.getOwner()) : "未知";
            setItem(slot, createItem(Material.NAME_TAG,
                    ChatColor.YELLOW + name,
                    ChatColor.GRAY + "宗主: " + ownerName,
                    ChatColor.GRAY + "点击加入"));
        }

        setItem(27, createItem(Material.ARROW,
                ChatColor.YELLOW + "上一页",
                ChatColor.GRAY + "上一页"));
        setItem(28, createItem(Material.ARROW,
                ChatColor.YELLOW + "下一页",
                ChatColor.GRAY + "下一页"));
        setItem(31, createItem(Material.BARRIER,
                ChatColor.YELLOW + "关闭",
                ChatColor.GRAY + "关闭界面"));

        ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, empty);
            }
        }
        for (int i = 29; i <= 35; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, empty);
            }
        }
    }

    private void buildConfirmDelete(String sectName) {
        setItem(13, createItem(Material.TNT,
                ChatColor.YELLOW + "确认删除宗门",
                ChatColor.GRAY + "宗门: " + sectName,
                ChatColor.GRAY + "此操作不可撤销！"));
        setItem(11, createItem(Material.EMERALD_BLOCK,
                ChatColor.YELLOW + "确认",
                ChatColor.GRAY + "点击确认删除"));
        setItem(15, createItem(Material.REDSTONE_BLOCK,
                ChatColor.YELLOW + "取消",
                ChatColor.GRAY + "点击取消操作"));
        fillEmptySlots();
    }

    private void buildWarInvite(String attacker, String defender) {
        setItem(13, createItem(Material.DIAMOND_SWORD,
                ChatColor.YELLOW + "宗门战邀请",
                ChatColor.GRAY + attacker + " -> " + defender,
                ChatColor.GRAY + "对方宗主确认后开战"));
        setItem(11, createItem(Material.EMERALD_BLOCK,
                ChatColor.YELLOW + "接受",
                ChatColor.GRAY + "同意开战"));
        setItem(15, createItem(Material.REDSTONE_BLOCK,
                ChatColor.YELLOW + "拒绝",
                ChatColor.GRAY + "拒绝开战"));
        fillEmptySlots();
    }

    private Sect findPlayerSect(UUID playerId) {
        for (Sect sect : dataManager.getSects().values()) {
            if (sect.getOwner().equals(playerId)) {
                return sect;
            }
            Map<UUID, String> members = sect.getMembers();
            if (members.containsKey(playerId)) {
                return sect;
            }
        }
        return null;
    }

    private String getName(UUID uuid) {
        org.bukkit.OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        return player.getName() != null ? player.getName() : uuid.toString();
    }

    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    private void fillEmptySlots() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
}