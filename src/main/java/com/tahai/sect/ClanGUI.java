package com.tahai.sect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClanGUI implements Listener, InventoryHolder {

    public enum GuiType { LIST, MANAGE }

    public static class GuiContext {
        public final GuiType type;
        public final int page;
        public final String clanName;

        public GuiContext(GuiType type, int page, String clanName) {
            this.type = type;
            this.page = page;
            this.clanName = clanName;
        }
    }

    private final ClanManager clanManager;
    private final Plugin plugin;
    private final Map<UUID, GuiContext> contexts = new HashMap<>();
    private Inventory inventory;

    public ClanGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
        this.plugin = Bukkit.getPluginManager().getPlugin("Sect");
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public GuiContext getContext(UUID uuid) {
        return contexts.get(uuid);
    }

    public void open(Player player) {
        openList(player, 0);
    }

    public void openList(Player player, int page) {
        clanManager.save();
        YamlConfiguration cfg = loadClansConfig();
        ConfigurationSection section = cfg.getConfigurationSection("clans");
        List<String> clans = new ArrayList<>();
        if (section != null) {
            clans.addAll(section.getKeys(false));
        }
        String myClan = clanManager.getClanName(player.getUniqueId());
        if (myClan != null && !myClan.isEmpty() && section != null && section.contains(myClan)) {
            clans.remove(myClan);
            clans.add(0, myClan);
        }
        int totalPages = Math.max(1, (int) Math.ceil(clans.size() / 45.0));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(this, 54, "宗门列表");
        int start = page * 45;
        for (int i = 0; i < 45; i++) {
            int index = start + i;
            if (index >= clans.size()) break;
            String name = clans.get(index);
            String owner = section.getString(name + ".owner", "");
            int level = section.getInt(name + ".level", 1);
            inv.setItem(i, createClanIcon(name, owner, level));
        }
        inv.setItem(45, page > 0 ? createButton(Material.ARROW, ChatColor.YELLOW + "上一页") : null);
        inv.setItem(46, createButton(Material.PAPER, ChatColor.YELLOW + "第 " + (page + 1) + "/" + totalPages + " 页"));
        inv.setItem(47, page + 1 < totalPages ? createButton(Material.ARROW, ChatColor.YELLOW + "下一页") : null);
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.YELLOW + "关闭"));

        this.inventory = inv;
        contexts.put(player.getUniqueId(), new GuiContext(GuiType.LIST, page, myClan));
        player.openInventory(inv);
    }

    public void openManage(Player player) {
        String clan = clanManager.getClanName(player.getUniqueId());
        if (clan == null || clan.isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "你还未加入任何宗门。");
            openList(player, 0);
            return;
        }
        Inventory inv = Bukkit.createInventory(this, 54, "宗门管理");
        ItemStack glass = createButton(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }
        inv.setItem(21, createButton(Material.OAK_SIGN, ChatColor.YELLOW + "邀请玩家", "点击邀请玩家加入宗门"));
        inv.setItem(22, createButton(Material.PLAYER_HEAD, ChatColor.YELLOW + "成员管理", "查看和管理宗门成员"));
        inv.setItem(23, createButton(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW + "升级宗门", "消耗金币提升宗门等级"));
        inv.setItem(30, createButton(Material.IRON_SWORD, ChatColor.YELLOW + "发起宗门战", "向其他宗门发起战争"));
        inv.setItem(31, createButton(Material.BARRIER, ChatColor.YELLOW + "解散宗门", "解散当前宗门"));
        inv.setItem(32, createButton(Material.WRITABLE_BOOK, ChatColor.YELLOW + "查看申请审批", "审批入宗申请"));
        inv.setItem(49, createButton(Material.ARROW, ChatColor.YELLOW + "返回列表", "返回宗门列表"));

        this.inventory = inv;
        contexts.put(player.getUniqueId(), new GuiContext(GuiType.MANAGE, 0, clan));
        player.openInventory(inv);
    }

    public void refresh(Player player) {
        GuiContext context = contexts.get(player.getUniqueId());
        if (context == null) {
            openList(player, 0);
            return;
        }
        if (context.type == GuiType.LIST) {
            openList(player, context.page);
        } else {
            openManage(player);
        }
    }

    private ItemStack createClanIcon(String clanName, String owner, int level) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (owner != null && !owner.isEmpty()) {
            try {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
            } catch (IllegalArgumentException e) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            }
        }
        meta.setDisplayName(ChatColor.YELLOW + clanName);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "宗主: " + ChatColor.WHITE + resolveName(owner));
        lore.add(ChatColor.GRAY + "等级: " + ChatColor.WHITE + level);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createButton(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (loreLines.length > 0) {
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(ChatColor.GRAY + line);
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private String resolveName(String owner) {
        if (owner == null || owner.isEmpty()) {
            return "未知";
        }
        try {
            UUID uuid = UUID.fromString(owner);
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            return offline.getName() != null ? offline.getName() : owner;
        } catch (IllegalArgumentException e) {
            return owner;
        }
    }

    private YamlConfiguration loadClansConfig() {
        if (plugin == null) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "clans.yml"));
    }
}