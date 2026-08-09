package com.tahai.sect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class SectCommand implements CommandExecutor, TabCompleter {

    private static GuildManager guildManager;
    private static final Map<String, String> pendingWars = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sect.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令。");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.GRAY + "用法: /sect create <name> | gui | war <target> | delete | accept <target>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreate(player, args);
            case "gui":
                return handleGui(player);
            case "war":
                return handleWar(player, args);
            case "delete":
                return handleDelete(player);
            case "accept":
                return handleAccept(player, args);
            default:
                player.sendMessage(ChatColor.GRAY + "未知子命令。用法: /sect create <name> | gui | war <target> | delete | accept <target>");
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.AQUA + "用法: /sect create <name>");
            return true;
        }
        if (getGuildManager().createGuild(player, args[1])) {
            getGuildManager().save();
            player.sendMessage(ChatColor.YELLOW + "宗门创建成功！");
        } else {
            player.sendMessage(ChatColor.AQUA + "宗门创建失败（名称已存在或你已经有宗门）。");
        }
        return true;
    }

    private boolean handleGui(Player player) {
        String guild = getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(ChatColor.AQUA + "你还没有宗门。");
            return true;
        }
        GuiHolder holder = new GuiHolder(ChatColor.YELLOW + "宗门面板");
        Inventory inv = holder.getInventory();
        inv.setItem(0, createInfoItem(Material.PAPER, ChatColor.YELLOW + "宗门: " + guild));
        inv.setItem(1, createInfoItem(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW + "等级: " + getGuildManager().getGuildLevel(guild)));
        inv.setItem(2, createInfoItem(Material.DIAMOND_SWORD, ChatColor.YELLOW + "击杀数: " + getGuildManager().getGuildKills(guild)));
        player.openInventory(inv);
        return true;
    }

    private boolean handleWar(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.AQUA + "用法: /sect war <target>");
            return true;
        }
        String attacker = getPlayerGuild(player);
        if (attacker == null) {
            player.sendMessage(ChatColor.AQUA + "你没有宗门，无法宣战。");
            return true;
        }
        String target = args[1];
        if (!getGuildManager().guildExists(target)) {
            player.sendMessage(ChatColor.AQUA + "目标宗门不存在。");
            return true;
        }
        if (attacker.equalsIgnoreCase(target)) {
            player.sendMessage(ChatColor.AQUA + "不能向自己的宗门宣战。");
            return true;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin != null && plugin.getConfig().contains("war.attacker")) {
            player.sendMessage(ChatColor.AQUA + "已有进行中的战争，无法再次宣战。");
            return true;
        }
        pendingWars.put(target, attacker);
        player.sendMessage(ChatColor.YELLOW + "已向宗门 " + target + " 发出宣战邀请，等待对方接受！");
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (target.equalsIgnoreCase(getPlayerGuild(online))) {
                online.sendMessage(ChatColor.YELLOW + "你的宗门被 " + attacker + " 宣战！使用 /sect accept " + attacker + " 应战。");
            }
        }
        return true;
    }

    private boolean handleDelete(Player player) {
        String guild = getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage(ChatColor.AQUA + "你没有宗门。");
            return true;
        }
        if (getGuildManager().deleteGuild(guild)) {
            getGuildManager().save();
            player.sendMessage(ChatColor.YELLOW + "宗门已解散。");
        } else {
            player.sendMessage(ChatColor.AQUA + "解散宗门失败（你可能不是宗主）。");
        }
        return true;
    }

    private boolean handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.AQUA + "用法: /sect accept <target>");
            return true;
        }
        String target = args[1];
        String playerGuild = getPlayerGuild(player);

        if (getGuildManager().guildExists(target) || pendingWars.containsValue(target)) {
            if (playerGuild == null) {
                player.sendMessage(ChatColor.AQUA + "你没有宗门，无法接受战争邀请。");
                return true;
            }
            String attacker = findWarAttacker(playerGuild, target);
            if (attacker == null) {
                player.sendMessage(ChatColor.AQUA + "没有来自该宗门的战争邀请。");
                return true;
            }
            if (getGuildManager().startWar(attacker, playerGuild)) {
                getGuildManager().save();
                pendingWars.remove(playerGuild);
                player.sendMessage(ChatColor.YELLOW + "战争已开始！");
            } else {
                player.sendMessage(ChatColor.AQUA + "开战失败。");
            }
            return true;
        }

        if (playerGuild == null) {
            player.sendMessage(ChatColor.AQUA + "你没有宗门，无法接受入宗申请。");
            return true;
        }
        if (getGuildManager().handleJoin(player, playerGuild, target, true)) {
            getGuildManager().save();
            player.sendMessage(ChatColor.YELLOW + "已接受 " + target + " 的入宗申请。");
        } else {
            player.sendMessage(ChatColor.AQUA + "接受入宗申请失败。");
        }
        return true;
    }

    private String findWarAttacker(String defenderGuild, String target) {
        for (Map.Entry<String, String> entry : pendingWars.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(defenderGuild) && entry.getValue().equalsIgnoreCase(target)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private GuildManager getGuildManager() {
        if (guildManager == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
            if (plugin == null) {
                throw new IllegalStateException("Sect plugin not found");
            }
            guildManager = new GuildManager(plugin);
        }
        return guildManager;
    }

    private String getPlayerGuild(Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) return null;
        ConfigurationSection guilds = plugin.getConfig().getConfigurationSection("guilds");
        String guild = findGuild(guilds, player);
        if (guild != null) return guild;
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (dataFile.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
            guild = findGuild(cfg.getConfigurationSection("guilds"), player);
        }
        return guild;
    }

    private String findGuild(ConfigurationSection guilds, Player player) {
        if (guilds == null) return null;
        String name = player.getName();
        String uuid = player.getUniqueId().toString();
        for (String guildName : guilds.getKeys(false)) {
            String leader = guilds.getString(guildName + ".leader");
            if (name.equals(leader) || uuid.equals(leader)) return guildName;
            ConfigurationSection members = guilds.getConfigurationSection(guildName + ".members");
            if (members != null) {
                for (String key : members.getKeys(false)) {
                    if (key.equals(name) || name.equals(members.getString(key))
                            || key.equals(uuid) || uuid.equals(members.getString(key))) {
                        return guildName;
                    }
                }
            }
        }
        return null;
    }

    private List<String> getGuildNames() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) return Collections.emptyList();
        List<String> names = new ArrayList<>();
        ConfigurationSection guilds = plugin.getConfig().getConfigurationSection("guilds");
        if (guilds != null) names.addAll(guilds.getKeys(false));
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (dataFile.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
            ConfigurationSection dataGuilds = cfg.getConfigurationSection("guilds");
            if (dataGuilds != null) {
                for (String guildName : dataGuilds.getKeys(false)) {
                    if (!names.contains(guildName)) names.add(guildName);
                }
            }
        }
        return names;
    }

    private ItemStack createInfoItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("sect.use")) return Collections.emptyList();
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("create", "gui", "war", "delete", "accept");
            return filter(subCommands, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("war") || sub.equals("accept")) {
                List<String> candidates = new ArrayList<>(getGuildNames());
                if (sub.equals("accept")) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        candidates.add(online.getName());
                    }
                }
                return filter(candidates, args[1]);
            }
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> candidates, String prefix) {
        List<String> result = new ArrayList<>();
        for (String s : candidates) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) result.add(s);
        }
        return result;
    }

    private static class GuiHolder implements InventoryHolder {
        private final Inventory inv;

        GuiHolder(String title) {
            this.inv = Bukkit.createInventory(this, 9, title);
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }
}