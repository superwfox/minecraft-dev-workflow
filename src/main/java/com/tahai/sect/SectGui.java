package com.tahai.sect;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class SectGui implements Listener, InventoryHolder {

    public enum GuiType {
        JOIN,
        MANAGE
    }

    public static final int JOIN_SIZE = 54;
    public static final int MANAGE_SIZE = 27;
    public static final int PAGE_SIZE = 45;
    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int NEXT_PAGE_SLOT = 53;
    public static final int INVITE_SLOT = 10;
    public static final int RANK_UP_SLOT = 12;
    public static final int LEVEL_UP_SLOT = 14;
    public static final int WAR_SLOT = 16;

    private final GuiType type;
    private final Player viewer;
    private final Inventory inventory;
    private final Plugin plugin;
    private DataManager dataManager;

    private List<String> sectNames = new ArrayList<>();
    private int currentPage = 0;
    private String clanName;

    public SectGui(Player viewer, GuiType type) {
        this(viewer, type, null);
    }

    public SectGui(Player viewer, GuiType type, String clanName) {
        this.viewer = viewer;
        this.type = type;
        this.clanName = clanName;
        this.plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (this.plugin != null) {
            this.dataManager = new DataManager(this.plugin);
            this.dataManager.load();
        }
        if (type == GuiType.JOIN) {
            this.sectNames = loadSectNames();
            this.inventory = Bukkit.createInventory(this, JOIN_SIZE, "宗门列表");
            buildCurrentPage();
        } else {
            this.inventory = Bukkit.createInventory(this, MANAGE_SIZE, "宗门管理");
            buildManage();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public GuiType getType() {
        return type;
    }

    public String getClanName() {
        return clanName;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getMaxPage() {
        if (type != GuiType.JOIN) {
            return 1;
        }
        return Math.max(1, (sectNames.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public void open(Player player) {
        refresh();
        player.openInventory(inventory);
    }

    public boolean nextPage() {
        if (type != GuiType.JOIN || currentPage >= getMaxPage() - 1) {
            return false;
        }
        currentPage++;
        buildCurrentPage();
        return true;
    }

    public boolean previousPage() {
        if (type != GuiType.JOIN || currentPage <= 0) {
            return false;
        }
        currentPage--;
        buildCurrentPage();
        return true;
    }

    public String getSectNameAt(int slot) {
        if (type != GuiType.JOIN || slot < 0 || slot >= PAGE_SIZE) {
            return null;
        }
        int index = currentPage * PAGE_SIZE + slot;
        if (index >= 0 && index < sectNames.size()) {
            return sectNames.get(index);
        }
        return null;
    }

    public SectClan getClan() {
        if (type != GuiType.MANAGE || clanName == null || dataManager == null) {
            return null;
        }
        return dataManager.getSect(clanName);
    }

    public void refresh() {
        if (dataManager == null) {
            return;
        }
        dataManager.load();
        if (type == GuiType.JOIN) {
            sectNames = loadSectNames();
            if (currentPage >= getMaxPage()) {
                currentPage = Math.max(0, getMaxPage() - 1);
            }
            buildCurrentPage();
        } else {
            buildManage();
        }
    }

    private void buildCurrentPage() {
        inventory.clear();
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < JOIN_SIZE; i++) {
            inventory.setItem(i, filler.clone());
        }
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, sectNames.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            inventory.setItem(slot, buildSectItem(sectNames.get(i)));
            slot++;
        }
        inventory.setItem(PREVIOUS_PAGE_SLOT, createItem(Material.ARROW, ChatColor.YELLOW + "上一页"));
        inventory.setItem(49, createItem(Material.PAPER, ChatColor.GRAY + "第 " + (currentPage + 1) + " / " + getMaxPage() + " 页"));
        inventory.setItem(NEXT_PAGE_SLOT, createItem(Material.ARROW, ChatColor.YELLOW + "下一页"));
    }

    private void buildManage() {
        inventory.clear();
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < MANAGE_SIZE; i++) {
            inventory.setItem(i, filler.clone());
        }
        SectClan clan = dataManager != null ? dataManager.getSect(clanName) : null;
        if (clan == null) {
            inventory.setItem(13, createItem(Material.BARRIER, ChatColor.AQUA + "宗门不存在"));
            return;
        }
        ItemStack info = createItem(Material.BOOK, ChatColor.YELLOW + clan.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "宗主: " + ChatColor.WHITE + playerName(clan.getLeaderUuid()));
        lore.add(ChatColor.GRAY + "等级: " + ChatColor.YELLOW + clan.getLevel());
        lore.add(ChatColor.GRAY + "成员: " + ChatColor.WHITE + clan.getMembers().size());
        lore.add(ChatColor.GRAY + "击杀数: " + ChatColor.WHITE + clan.getKillCount());
        lore.add(ChatColor.GRAY + "世界: " + ChatColor.WHITE + (clan.getWorld() != null ? clan.getWorld() : "未知"));
        if (clan.getRegionName() != null && !clan.getRegionName().isEmpty()) {
            lore.add(ChatColor.GRAY + "领地: " + ChatColor.WHITE + clan.getRegionName());
        }
        setLore(info, lore);
        inventory.setItem(4, info);

        inventory.setItem(INVITE_SLOT, createItem(Material.NAME_TAG, ChatColor.YELLOW + "邀请",
                ChatColor.GRAY + "邀请玩家加入宗门"));
        inventory.setItem(RANK_UP_SLOT, createItem(Material.EMERALD, ChatColor.YELLOW + "职位升级",
                ChatColor.GRAY + "提升一名成员的职位"));
        inventory.setItem(LEVEL_UP_SLOT, createItem(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW + "等级升级",
                ChatColor.GRAY + "消耗资源升级宗门等级"));
        inventory.setItem(WAR_SLOT, createItem(Material.IRON_SWORD, ChatColor.YELLOW + "宗门战",
                ChatColor.GRAY + "向其他宗门发起战斗"));
    }

    private ItemStack buildSectItem(String name) {
        SectClan clan = dataManager != null ? dataManager.getSect(name) : null;
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + (clan != null ? clan.getName() : name));
        List<String> lore = new ArrayList<>();
        String leader = "?";
        int level = 1;
        int kills = 0;
        int members = 0;
        if (clan != null) {
            leader = playerName(clan.getLeaderUuid());
            level = clan.getLevel();
            kills = clan.getKillCount();
            members = clan.getMembers().size();
        }
        lore.add(ChatColor.GRAY + "宗主: " + leader);
        lore.add(ChatColor.GRAY + "等级: " + ChatColor.YELLOW + level);
        lore.add(ChatColor.GRAY + "成员: " + ChatColor.WHITE + members);
        lore.add(ChatColor.GRAY + "击杀数: " + ChatColor.WHITE + kills);
        lore.add(ChatColor.YELLOW + "点击加入");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> loadSectNames() {
        List<String> names = new ArrayList<>();
        if (plugin == null) {
            return names;
        }
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            return names;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (cfg.isConfigurationSection("clans")) {
            names.addAll(cfg.getConfigurationSection("clans").getKeys(false));
        }
        Collections.sort(names);
        return names;
    }

    private String playerName(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() != null ? player.getName() : uuid.toString();
    }

    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (loreLines.length > 0) {
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void setLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}