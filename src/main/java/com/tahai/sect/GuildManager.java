package com.tahai.sect;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GuildManager {
    private final Plugin plugin;
    private final File dataFile;
    private final YamlConfiguration data;

    private static final double[] LEVEL_COSTS = {
            100_000_000D, 200_000_000D, 300_000_000D,
            400_000_000D, 500_000_000D, 600_000_000D
    };
    private static final String[] RANKS = {"LEADER", "VICE", "ELDER", "ELITE", "MEMBER"};

    public GuildManager(Plugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "guilds.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public synchronized void save() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save guilds.yml: " + e.getMessage());
        }
    }

    public synchronized void shutdown() {
        save();
    }

    public synchronized boolean createGuild(Player creator, String name) {
        if (name == null || !name.matches("[a-zA-Z0-9_]+")) return false;
        String path = "guilds." + name;
        if (data.contains(path)) return false;
        if (findGuildByPlayer(creator.getUniqueId().toString()) != null) return false;

        Economy economy = getEconomy();
        if (economy == null) return false;
        if (!economy.has(creator, LEVEL_COSTS[0])) return false;
        if (!economy.withdrawPlayer(creator, LEVEL_COSTS[0]).transactionSuccess()) return false;
        if (!createRegion(name, creator.getLocation())) {
            economy.depositPlayer(creator, LEVEL_COSTS[0]);
            return false;
        }

        data.set(path + ".leader", creator.getUniqueId().toString());
        data.set(path + ".level", 1);
        data.set(path + ".regionWorld", creator.getWorld().getName());
        data.set(path + ".kills", 0);
        data.set(path + ".coinRewardTime", 0L);
        data.set(path + ".members." + creator.getUniqueId().toString(), "LEADER");
        save();
        return true;
    }

    public synchronized boolean deleteGuild(String name) {
        String path = "guilds." + name;
        if (!data.contains(path)) return false;
        deleteRegion(name);
        data.set(path, null);
        save();
        return true;
    }

    public synchronized boolean applyJoin(Player player, String guildName) {
        String path = "guilds." + guildName;
        if (!data.contains(path)) return false;
        if (findGuildByPlayer(player.getUniqueId().toString()) != null) return false;

        List<String> applications = data.getStringList(path + ".applications");
        String uuid = player.getUniqueId().toString();
        if (applications.contains(uuid)) return false;
        applications.add(uuid);
        data.set(path + ".applications", applications);
        save();
        return true;
    }

    public synchronized boolean handleJoin(Player admin, String guildName, String applicantName, boolean accept) {
        String path = "guilds." + guildName;
        if (!data.contains(path)) return false;
        if (!isOfficer(path, admin.getUniqueId().toString())) return false;

        List<String> applications = data.getStringList(path + ".applications");
        if (applications.isEmpty()) return false;

        OfflinePlayer applicant = Bukkit.getOfflinePlayer(applicantName);
        String applicantUuid = applicant.getUniqueId().toString();
        if (!applications.remove(applicantUuid)) return false;
        data.set(path + ".applications", applications);

        if (accept) {
            data.set(path + ".members." + applicantUuid, "MEMBER");
        }
        save();
        return true;
    }

    public synchronized boolean promoteMember(Player operator, String guildName, Player target) {
        String path = "guilds." + guildName;
        if (!data.contains(path)) return false;
        if (!isOfficer(path, operator.getUniqueId().toString())) return false;

        String targetUuid = target.getUniqueId().toString();
        String current = data.getString(path + ".members." + targetUuid);
        if (current == null) return false;

        int index = Arrays.asList(RANKS).indexOf(current);
        if (index <= 0) return false;
        data.set(path + ".members." + targetUuid, RANKS[index - 1]);
        save();
        return true;
    }

    public synchronized boolean upgradeGuild(Player operator, String guildName) {
        String path = "guilds." + guildName;
        if (!data.contains(path)) return false;
        if (!isOfficer(path, operator.getUniqueId().toString())) return false;

        int level = data.getInt(path + ".level", 1);
        if (level >= 6) return false;

        double cost = LEVEL_COSTS[level];
        Economy economy = getEconomy();
        if (economy == null) return false;
        if (!economy.has(operator, cost)) return false;
        if (!economy.withdrawPlayer(operator, cost).transactionSuccess()) return false;

        data.set(path + ".level", level + 1);
        save();
        return true;
    }

    public synchronized boolean startWar(String attackerGuild, String defenderGuild) {
        if (attackerGuild.equalsIgnoreCase(defenderGuild)) return false;
        if (!data.contains("guilds." + attackerGuild) || !data.contains("guilds." + defenderGuild)) return false;
        if (data.contains("war")) return false;

        data.set("war.attacker", attackerGuild);
        data.set("war.defender", defenderGuild);
        data.set("war.startTime", System.currentTimeMillis());
        data.set("war.attackerStartKills", data.getInt("guilds." + attackerGuild + ".kills", 0));
        data.set("war.defenderStartKills", data.getInt("guilds." + defenderGuild + ".kills", 0));
        save();
        return true;
    }

    public synchronized String endWar() {
        if (!data.contains("war")) return null;

        String attacker = data.getString("war.attacker");
        String defender = data.getString("war.defender");
        if (attacker == null || defender == null) {
            data.set("war", null);
            save();
            return null;
        }

        int attackerDiff = data.getInt("guilds." + attacker + ".kills", 0) - data.getInt("war.attackerStartKills", 0);
        int defenderDiff = data.getInt("guilds." + defender + ".kills", 0) - data.getInt("war.defenderStartKills", 0);
        String winner = attackerDiff > defenderDiff ? attacker : (defenderDiff > attackerDiff ? defender : "DRAW");

        data.set("war", null);
        save();
        return winner;
    }

    public synchronized void recordCoinRewardTime(String guildName) {
        String path = "guilds." + guildName;
        if (data.contains(path)) {
            data.set(path + ".coinRewardTime", System.currentTimeMillis() + 12L * 60 * 60 * 1000);
            save();
        }
    }

    public synchronized void addGuildKill(String guildName, int amount) {
        String path = "guilds." + guildName;
        if (data.contains(path)) {
            data.set(path + ".kills", data.getInt(path + ".kills", 0) + amount);
            save();
        }
    }

    public synchronized boolean guildExists(String guildName) {
        return data.contains("guilds." + guildName);
    }

    public synchronized int getGuildLevel(String guildName) {
        return data.getInt("guilds." + guildName + ".level", 1);
    }

    public synchronized int getGuildKills(String guildName) {
        return data.getInt("guilds." + guildName + ".kills", 0);
    }

    private String findGuildByPlayer(String playerUuid) {
        if (!data.contains("guilds")) return null;
        for (String guildName : data.getConfigurationSection("guilds").getKeys(false)) {
            if (data.contains("guilds." + guildName + ".members." + playerUuid)) {
                return guildName;
            }
        }
        return null;
    }

    private boolean isOfficer(String path, String playerUuid) {
        String rank = data.getString(path + ".members." + playerUuid);
        return "LEADER".equals(rank) || "VICE".equals(rank);
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        return provider == null ? null : provider.getProvider();
    }

    private boolean createRegion(String id, Location location) {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return false;
        World world = BukkitAdapter.adapt(location.getWorld());
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (manager == null) return false;
        if (manager.getRegion(id) != null) return false;

        int radius = 16;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        BlockVector3 min = BlockVector3.at(x - radius, Math.max(0, y - radius), z - radius);
        BlockVector3 max = BlockVector3.at(x + radius, Math.min(255, y + radius), z + radius);
        manager.addRegion(new ProtectedCuboidRegion(id, min, max));
        return manager.getRegion(id) != null;
    }

    private boolean deleteRegion(String id) {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return false;
        String worldName = data.getString("guilds." + id + ".regionWorld");
        if (worldName == null) return false;

        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return false;

        World world = BukkitAdapter.adapt(bukkitWorld);
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (manager == null) return false;
        manager.removeRegion(id);
        return true;
    }
}