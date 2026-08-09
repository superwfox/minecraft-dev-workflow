package com.tahai.sect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ManageSectGuiHolder implements Listener, InventoryHolder {

    public static final int SLOT_INVITE = 10;
    public static final int SLOT_UPGRADE = 11;
    public static final int SLOT_POSITION = 12;
    public static final int SLOT_WAR = 13;
    public static final int SLOT_DISBAND = 14;

    private final Inventory inv;
    private final Player player;
    private final String guildName;
    private final Plugin plugin;
    private final GuildManager guildManager;

    public ManageSectGuiHolder(Player player, String guildName) {
        this.player = player;
        this.guildName = guildName;
        this.plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            throw new IllegalStateException("Sect plugin not found");
        }
        this.guildManager = new GuildManager(plugin);
        this.inv = Bukkit.createInventory(this, 27, "宗门管理 - " + guildName);
        fillInventory();
    }

    public void open(Player player) {
        player.openInventory(inv);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public String getGuildName() {
        return guildName;
    }

    public Player getPlayer() {
        return player;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    private void fillInventory() {
        YamlConfiguration data = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data.yml"));
        ConfigurationSection guild = data.getConfigurationSection("guilds." + guildName);
        String leader = guild == null ? "" : guild.getString("leader", "");
        int level = guildManager.getGuildLevel(guildName);
        int kills = guildManager.getGuildKills(guildName);
        int memberCount = 0;
        if (guild != null && guild.getConfigurationSection("members") != null) {
            memberCount = guild.getConfigurationSection("members").getKeys(false).size();
        }

        String warStatus = "无";
        String attacker = data.getString("war.attacker", "");
        String defender = data.getString("war.defender", "");
        if (guildName.equals(attacker) || guildName.equals(defender)) {
            warStatus = "战争中";
        }

        inv.setItem(4, createInfoItem(leader, level, kills, memberCount, warStatus));

        boolean isLeader = leader.equals(player.getName()) || leader.equals(player.getUniqueId().toString());
        if (isLeader) {
            inv.setItem(SLOT_INVITE, createButton(Material.NAME_TAG, ChatColor.YELLOW + "邀请成员", ChatColor.GRAY + "点击邀请玩家加入宗门"));
            inv.setItem(SLOT_UPGRADE, createButton(Material.ANVIL, ChatColor.YELLOW + "升级宗门", ChatColor.GRAY + "提升宗门等级"));
            inv.setItem(SLOT_POSITION, createButton(Material.ARMOR_STAND, ChatColor.YELLOW + "调整职位", ChatColor.GRAY + "管理成员职位"));
            inv.setItem(SLOT_WAR, createButton(Material.DIAMOND_SWORD, ChatColor.YELLOW + "发起宗门战", ChatColor.GRAY + "向其他宗门宣战"));
            inv.setItem(SLOT_DISBAND, createButton(Material.BARRIER, ChatColor.YELLOW + "解散宗门", ChatColor.GRAY + "永久解散当前宗门"));
        } else {
            inv.setItem(SLOT_WAR, createButton(Material.PAPER, ChatColor.GRAY + "当前状态", ChatColor.GRAY + "你是宗门成员"));
        }

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    private ItemStack createInfoItem(String leader, int level, int kills, int members, String warStatus) {
        ItemStack item = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "宗门信息");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "宗主: " + leader);
        lore.add(ChatColor.GRAY + "等级: " + level);
        lore.add(ChatColor.GRAY + "击杀: " + kills);
        lore.add(ChatColor.GRAY + "成员: " + members);
        lore.add(ChatColor.GRAY + "战争: " + warStatus);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createButton(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> list = new ArrayList<>();
        list.add(lore);
        meta.setLore(list);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}