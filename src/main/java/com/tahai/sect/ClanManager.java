package com.tahai.sect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ClanManager {

    private final Plugin plugin;
    private File dataFile;
    private YamlConfiguration data;
    private Economy economy;
    private Map<UUID, String> playerClans = new HashMap<>();
    private BukkitTask warTask;

    private static final double CLAN_CREATE_COST = 100000000D;
    private static final double PROMOTE_ELITE_COST = 10000000D;
    private static final double PROMOTE_VICE_COST = 50000000D;
    private static final double WAR_REWARD = 6000000000D;
    private static final long WAR_DURATION_TICKS = 30 * 60 * 20L;
    private static final long INVITE_EXPIRY_MS = 2 * 60 * 1000L;

    public ClanManager(Plugin plugin) {
        this.plugin = plugin;
        load();
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    private void load() {
        dataFile = new File(plugin.getDataFolder(), "clans.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("无法创建 clans.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        if (!data.isConfigurationSection("clans")) {
            data.createSection("clans");
        }
        if (!data.isConfigurationSection("war")) {
            ConfigurationSection war = data.createSection("war");
            war.set("state", "NONE");
        }
        playerClans.clear();
        ConfigurationSection clans = data.getConfigurationSection("clans");
        if (clans != null) {
            for (String clanName : clans.getKeys(false)) {
                ConfigurationSection roles = data.getConfigurationSection("clans." + clanName + ".roles");
                if (roles != null) {
                    for (String key : roles.getKeys(false)) {
                        try {
                            playerClans.put(UUID.fromString(key), clanName);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        }
    }

    public void save() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存 clans.yml: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (warTask != null) {
            warTask.cancel();
            warTask = null;
        }
        save();
    }

    public String getClanName(UUID playerId) {
        return playerClans.get(playerId);
    }

    public String getRole(UUID playerId) {
        String clanName = playerClans.get(playerId);
        if (clanName == null) return null;
        String role = data.getString("clans." + clanName + ".roles." + playerId.toString());
        if (role == null) return null;
        switch (role) {
            case "owner":
                return "宗主";
            case "vice":
                return "副宗主";
            case "elite":
                return "精英";
            case "member":
                return "普通";
            default:
                return role;
        }
    }

    public boolean createClan(Player player, String clanName) {
        if (playerClans.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.AQUA + "你已加入宗门，不能创建新宗门");
            return false;
        }
        if (data.getConfigurationSection("clans." + clanName) != null) {
            player.sendMessage(ChatColor.AQUA + "宗门名称已存在");
            return false;
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            player.sendMessage(ChatColor.AQUA + "WorldGuard 未启用");
            return false;
        }
        if (economy == null) {
            player.sendMessage(ChatColor.AQUA + "未找到经济插件");
            return false;
        }
        if (economy.getBalance(player) < CLAN_CREATE_COST) {
            player.sendMessage(ChatColor.AQUA + "创建宗门需要 " + economy.format(CLAN_CREATE_COST));
            return false;
        }
        Region sel;
        try {
            sel = getSelection(player);
        } catch (Exception e) {
            player.sendMessage(ChatColor.AQUA + "请先用WorldEdit选择一个区域");
            return false;
        }
        EconomyResponse withdraw = economy.withdrawPlayer(player, CLAN_CREATE_COST);
        if (!withdraw.transactionSuccess()) {
            player.sendMessage(ChatColor.AQUA + "扣款失败: " + withdraw.errorMessage);
            return false;
        }
        try {
            createWGRegion(player, clanName, sel);
        } catch (Exception e) {
            economy.depositPlayer(player, CLAN_CREATE_COST);
            player.sendMessage(ChatColor.AQUA + "创建WorldGuard区域失败，已退款");
            return false;
        }
        ConfigurationSection clanSec = data.getConfigurationSection("clans").createSection(clanName);
        clanSec.set("owner", player.getUniqueId().toString());
        clanSec.set("world", player.getWorld().getName());
        clanSec.set("level", 1);
        ConfigurationSection roles = clanSec.createSection("roles");
        roles.set(player.getUniqueId().toString(), "owner");
        clanSec.set("pending", new ArrayList<String>());
        playerClans.put(player.getUniqueId(), clanName);
        save();
        player.sendMessage(ChatColor.YELLOW + "宗门 " + clanName + " 创建成功");
        return true;
    }

    public boolean requestJoin(Player player, String clanName) {
        if (playerClans.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.AQUA + "你已加入宗门");
            return false;
        }
        if (data.getConfigurationSection("clans." + clanName) == null) {
            player.sendMessage(ChatColor.AQUA + "宗门不存在");
            return false;
        }
        List<String> pending = data.getStringList("clans." + clanName + ".pending");
        if (pending.contains(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.AQUA + "你已申请过该宗门");
            return false;
        }
        pending.add(player.getUniqueId().toString());
        data.set("clans." + clanName + ".pending", pending);
        save();
        player.sendMessage(ChatColor.YELLOW + "入宗申请已提交");
        return true;
    }

    public boolean approveJoin(Player owner, String playerName) {
        String clanName = getClanName(owner.getUniqueId());
        if (clanName == null || !isOwner(owner, clanName)) {
            owner.sendMessage(ChatColor.AQUA + "你不是宗主");
            return false;
        }
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            owner.sendMessage(ChatColor.AQUA + "玩家不在线");
            return false;
        }
        if (playerClans.containsKey(target.getUniqueId())) {
            owner.sendMessage(ChatColor.AQUA + "该玩家已有宗门");
            return false;
        }
        List<String> pending = data.getStringList("clans." + clanName + ".pending");
        if (!pending.remove(target.getUniqueId().toString())) {
            owner.sendMessage(ChatColor.AQUA + "该玩家没有申请记录");
            return false;
        }
        data.set("clans." + clanName + ".pending", pending);
        data.set("clans." + clanName + ".roles." + target.getUniqueId().toString(), "member");
        playerClans.put(target.getUniqueId(), clanName);
        addMemberToWGRegion(clanName, target.getUniqueId());
        save();
        target.sendMessage(ChatColor.YELLOW + "你已加入宗门 " + clanName);
        owner.sendMessage(ChatColor.YELLOW + "已同意 " + target.getName() + " 加入宗门");
        return true;
    }

    public boolean denyJoin(Player owner, String playerName) {
        String clanName = getClanName(owner.getUniqueId());
        if (clanName == null || !isOwner(owner, clanName)) {
            owner.sendMessage(ChatColor.AQUA + "你不是宗主");
            return false;
        }
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            owner.sendMessage(ChatColor.AQUA + "玩家不在线");
            return false;
        }
        List<String> pending = data.getStringList("clans." + clanName + ".pending");
        if (!pending.remove(target.getUniqueId().toString())) {
            owner.sendMessage(ChatColor.AQUA + "该玩家没有申请记录");
            return false;
        }
        data.set("clans." + clanName + ".pending", pending);
        save();
        target.sendMessage(ChatColor.AQUA + "你的入宗申请被拒绝");
        owner.sendMessage(ChatColor.YELLOW + "已拒绝 " + target.getName() + " 的申请");
        return true;
    }

    public boolean promote(Player player) {
        String clanName = getClanName(player.getUniqueId());
        if (clanName == null) {
            player.sendMessage(ChatColor.AQUA + "你尚未加入宗门");
            return false;
        }
        String role = data.getString("clans." + clanName + ".roles." + player.getUniqueId().toString());
        double cost = 0;
        String newRole = null;
        if ("member".equals(role)) {
            cost = PROMOTE_ELITE_COST;
            newRole = "elite";
        } else if ("elite".equals(role)) {
            cost = PROMOTE_VICE_COST;
            newRole = "vice";
        } else {
            player.sendMessage(ChatColor.AQUA + "你无法继续晋升");
            return false;
        }
        if (economy == null) {
            player.sendMessage(ChatColor.AQUA + "未找到经济插件");
            return false;
        }
        if (economy.getBalance(player) < cost) {
            player.sendMessage(ChatColor.AQUA + "晋升需要 " + economy.format(cost));
            return false;
        }
        EconomyResponse r = economy.withdrawPlayer(player, cost);
        if (!r.transactionSuccess()) {
            player.sendMessage(ChatColor.AQUA + "扣款失败: " + r.errorMessage);
            return false;
        }
        data.set("clans." + clanName + ".roles." + player.getUniqueId().toString(), newRole);
        save();
        player.sendMessage(ChatColor.YELLOW + "晋升成功");
        return true;
    }

    public boolean upgradeClan(Player player) {
        String clanName = getClanName(player.getUniqueId());
        if (clanName == null) {
            player.sendMessage(ChatColor.AQUA + "你尚未加入宗门");
            return false;
        }
        int level = data.getInt("clans." + clanName + ".level", 1);
        if (level >= 7) {
            player.sendMessage(ChatColor.AQUA + "宗门等级已满级");
            return false;
        }
        double cost = level * 100000000D;
        if (economy == null) {
            player.sendMessage(ChatColor.AQUA + "未找到经济插件");
            return false;
        }
        if (economy.getBalance(player) < cost) {
            player.sendMessage(ChatColor.AQUA + "升级需要 " + economy.format(cost));
            return false;
        }
        EconomyResponse r = economy.withdrawPlayer(player, cost);
        if (!r.transactionSuccess()) {
            player.sendMessage(ChatColor.AQUA + "扣款失败: " + r.errorMessage);
            return false;
        }
        data.set("clans." + clanName + ".level", level + 1);
        save();
        player.sendMessage(ChatColor.YELLOW + "宗门等级提升至 " + (level + 1));
        return true;
    }

    public boolean inviteWar(Player player, String targetClan) {
        String clanName = getClanName(player.getUniqueId());
        if (clanName == null || !isOwner(player, clanName)) {
            player.sendMessage(ChatColor.AQUA + "只有宗主才能发起宗门战");
            return false;
        }
        if (data.getConfigurationSection("clans." + targetClan) == null) {
            player.sendMessage(ChatColor.AQUA + "目标宗门不存在");
            return false;
        }
        if (clanName.equals(targetClan)) {
            player.sendMessage(ChatColor.AQUA + "不能向自己宗门宣战");
            return false;
        }
        clearExpiredWarIfNeeded();
        ConfigurationSection war = data.getConfigurationSection("war");
        String state = war.getString("state", "NONE");
        if (!"NONE".equals(state)) {
            player.sendMessage(ChatColor.AQUA + "当前已有宗门战进行或邀请中");
            return false;
        }
        war.set("state", "INVITED");
        war.set("inviter", clanName);
        war.set("target", targetClan);
        war.set("expiry", System.currentTimeMillis() + INVITE_EXPIRY_MS);
        save();
        String targetOwner = data.getString("clans." + targetClan + ".owner");
        if (targetOwner != null) {
            Player targetPlayer = Bukkit.getPlayer(UUID.fromString(targetOwner));
            if (targetPlayer != null) {
                targetPlayer.sendMessage(ChatColor.YELLOW + "宗门 " + clanName + " 邀请你进行宗门战，使用 /sect war accept 接受");
            }
        }
        player.sendMessage(ChatColor.YELLOW + "宗门战邀请已发送");
        return true;
    }

    public boolean acceptWar(Player player) {
        String clanName = getClanName(player.getUniqueId());
        if (clanName == null || !isOwner(player, clanName)) {
            player.sendMessage(ChatColor.AQUA + "只有宗主才能接受宗门战");
            return false;
        }
        ConfigurationSection war = data.getConfigurationSection("war");
        if (!"INVITED".equals(war.getString("state"))) {
            player.sendMessage(ChatColor.AQUA + "当前没有待接受的邀请");
            return false;
        }
        if (!clanName.equals(war.getString("target"))) {
            player.sendMessage(ChatColor.AQUA + "此邀请不是发给你的宗门");
            return false;
        }
        if (System.currentTimeMillis() > war.getLong("expiry")) {
            resetWar();
            save();
            player.sendMessage(ChatColor.AQUA + "邀请已过期");
            return false;
        }
        String inviter = war.getString("inviter");
        war.set("state", "ACTIVE");
        war.set("startTime", System.currentTimeMillis());
        ConfigurationSection kills = war.createSection("kills");
        kills.set(inviter, 0);
        kills.set(clanName, 0);
        warTask = new BukkitRunnable() {
            @Override
            public void run() {
                endWar();
            }
        }.runTaskLater(plugin, WAR_DURATION_TICKS);
        save();
        Bukkit.broadcastMessage(ChatColor.YELLOW + "宗门战开始！" + inviter + " vs " + clanName + "，30分钟后结算");
        return true;
    }

    public void onPlayerKill(Player killer, Player victim) {
        ConfigurationSection war = data.getConfigurationSection("war");
        if (!"ACTIVE".equals(war.getString("state"))) return;
        String killerClan = getClanName(killer.getUniqueId());
        String victimClan = getClanName(victim.getUniqueId());
        if (killerClan == null || victimClan == null) return;
        String inviter = war.getString("inviter");
        String target = war.getString("target");
        if ((killerClan.equals(inviter) && victimClan.equals(target)) || (killerClan.equals(target) && victimClan.equals(inviter))) {
            ConfigurationSection kills = war.getConfigurationSection("kills");
            if (kills == null) {
                kills = war.createSection("kills");
            }
            kills.set(killerClan, kills.getInt(killerClan) + 1);
            save();
        }
    }

    public boolean disbandClan(Player player) {
        String clanName = getClanName(player.getUniqueId());
        if (clanName == null || !isOwner(player, clanName)) {
            player.sendMessage(ChatColor.AQUA + "只有宗主才能解散宗门");
            return false;
        }
        ConfigurationSection war = data.getConfigurationSection("war");
        if (!"NONE".equals(war.getString("state"))) {
            player.sendMessage(ChatColor.AQUA + "宗门战期间不能解散宗门");
            return false;
        }
        disbandClanInternal(clanName);
        save();
        player.sendMessage(ChatColor.YELLOW + "宗门已解散");
        return true;
    }

    private boolean isOwner(Player player, String clanName) {
        String role = data.getString("clans." + clanName + ".roles." + player.getUniqueId().toString());
        return "owner".equals(role);
    }

    private void createWGRegion(Player player, String clanName, Region sel) throws Exception {
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(player.getWorld());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(weWorld);
        if (manager == null) {
            throw new Exception("RegionManager not available");
        }
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(clanName, sel.getMinimumPoint(), sel.getMaximumPoint());
        region.getOwners().addPlayer(player.getUniqueId());
        region.getMembers().addPlayer(player.getUniqueId());
        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.DENY);
        region.setFlag(Flags.USE, StateFlag.State.DENY);
        manager.addRegion(region);
    }

    private Region getSelection(Player player) throws Exception {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(player.getWorld());
        return session.getSelection(weWorld);
    }

    private RegionManager getRegionManager(String clanName) {
        String worldName = data.getString("clans." + clanName + ".world");
        if (worldName == null) return null;
        World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return null;
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(weWorld);
    }

    private void addMemberToWGRegion(String clanName, UUID uuid) {
        RegionManager manager = getRegionManager(clanName);
        if (manager != null) {
            ProtectedRegion region = manager.getRegion(clanName);
            if (region != null) {
                region.getMembers().addPlayer(uuid);
            }
        }
    }

    private void removeWGRegion(String clanName) {
        RegionManager manager = getRegionManager(clanName);
        if (manager != null) {
            manager.removeRegion(clanName);
        }
    }

    private void endWar() {
        ConfigurationSection war = data.getConfigurationSection("war");
        if (!"ACTIVE".equals(war.getString("state"))) return;
        String inviter = war.getString("inviter");
        String target = war.getString("target");
        int killsInviter = war.getInt("kills." + inviter, 0);
        int killsTarget = war.getInt("kills." + target, 0);

        if (killsInviter == killsTarget) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "宗门战平局！双方均存活");
        } else {
            String winner = killsInviter > killsTarget ? inviter : target;
            String loser = killsInviter > killsTarget ? target : inviter;
            String winnerOwner = data.getString("clans." + winner + ".owner");
            if (economy != null && winnerOwner != null) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(winnerOwner));
                economy.depositPlayer(op, WAR_REWARD);
            }
            disbandClanInternal(loser);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "宗门战结束！" + winner + " 获胜并获得60亿金币，" + loser + " 被解散");
        }
        resetWar();
        save();
    }

    private void resetWar() {
        ConfigurationSection war = data.getConfigurationSection("war");
        war.set("state", "NONE");
        war.set("inviter", null);
        war.set("target", null);
        war.set("expiry", null);
        war.set("startTime", null);
        war.set("kills", null);
        if (warTask != null) {
            warTask.cancel();
            warTask = null;
        }
    }

    private void clearExpiredWarIfNeeded() {
        ConfigurationSection war = data.getConfigurationSection("war");
        if ("INVITED".equals(war.getString("state"))) {
            if (System.currentTimeMillis() > war.getLong("expiry")) {
                resetWar();
                save();
            }
        }
    }

    private void disbandClanInternal(String clanName) {
        ConfigurationSection clanSec = data.getConfigurationSection("clans." + clanName);
        if (clanSec == null) return;
        ConfigurationSection roles = clanSec.getConfigurationSection("roles");
        if (roles != null) {
            for (String key : roles.getKeys(false)) {
                try {
                    playerClans.remove(UUID.fromString(key));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        removeWGRegion(clanName);
        data.set("clans." + clanName, null);
        save();
    }
}