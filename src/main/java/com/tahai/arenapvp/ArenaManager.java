package com.tahai.arenapvp;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private final Plugin plugin;
    private final Map<String, Game> games = new HashMap<>();

    private File mapsFile, kitsFile, statsFile;
    private YamlConfiguration mapsConfig, kitsConfig, statsConfig;

    public ArenaManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        plugin.getDataFolder().mkdirs();
        mapsFile = new File(plugin.getDataFolder(), "maps.yml");
        kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        statsFile = new File(plugin.getDataFolder(), "statistics.yml");
        if (!mapsFile.exists()) plugin.saveResource("maps.yml", false);
        if (!kitsFile.exists()) plugin.saveResource("kits.yml", false);
        if (!statsFile.exists()) plugin.saveResource("statistics.yml", false);
        mapsConfig = YamlConfiguration.loadConfiguration(mapsFile);
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    public void save() {
        try {
            mapsConfig.save(mapsFile);
            kitsConfig.save(kitsFile);
            statsConfig.save(statsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        for (Game game : new ArrayList<>(games.values())) {
            game.endGame();
        }
        save();
    }

    public Game getGame(String name) {
        return games.get(name);
    }

    public Collection<Game> getGames() {
        return Collections.unmodifiableCollection(games.values());
    }

    public void createGame(String name, World world, List<Location> spawns, int maxPlayers) {
        Game game = new Game(name, world, spawns, maxPlayers);
        games.put(name, game);
    }

    public void removeGame(String name) {
        Game game = games.remove(name);
        if (game != null) game.endGame();
    }

    // Statistics
    public int getStat(UUID uuid, String key) {
        return statsConfig.getInt(uuid.toString() + "." + key, 0);
    }

    public void addStat(UUID uuid, String key, int amount) {
        String path = uuid.toString() + "." + key;
        statsConfig.set(path, statsConfig.getInt(path, 0) + amount);
    }

    public void setStat(UUID uuid, String key, int value) {
        statsConfig.set(uuid.toString() + "." + key, value);
    }

    public class Game {
        private final String name;
        private final World world;
        private final List<Location> spawns;
        private final int maxPlayers;
        private GameState state = GameState.WAITING;
        private final Set<UUID> players = new HashSet<>();
        private final Map<UUID, Team> playerTeam = new HashMap<>();
        private final Map<UUID, Integer> lives = new HashMap<>();
        private int countdownTaskId = -1;
        private int gameTaskId = -1;
        private WorldGuardProtection protection;

        public enum GameState { WAITING, COUNTDOWN, PLAYING, ENDING }
        public enum Team { RED, BLUE, SOLO }

        public Game(String name, World world, List<Location> spawns, int maxPlayers) {
            this.name = name;
            this.world = world;
            this.spawns = spawns;
            this.maxPlayers = maxPlayers;
        }

        public String getName() { return name; }
        public GameState getState() { return state; }
        public World getWorld() { return world; }
        public Set<UUID> getPlayers() { return Collections.unmodifiableSet(players); }
        public int getPlayerCount() { return players.size(); }

        public void addPlayer(Player player) {
            if (players.size() >= maxPlayers || state != GameState.WAITING) return;
            players.add(player.getUniqueId());
            playerTeam.put(player.getUniqueId(), assignTeam());
            lives.put(player.getUniqueId(), 3);
            player.teleport(spawns.get(0));
            if (players.size() == maxPlayers) startCountdown();
        }

        public void removePlayer(Player player) {
            players.remove(player.getUniqueId());
            playerTeam.remove(player.getUniqueId());
            lives.remove(player.getUniqueId());
            if (state == GameState.PLAYING && players.size() <= 1) endGame();
            if (state == GameState.COUNTDOWN && players.size() < maxPlayers) cancelCountdown();
        }

        private Team assignTeam() {
            long red = playerTeam.values().stream().filter(t -> t == Team.RED).count();
            long blue = playerTeam.values().stream().filter(t -> t == Team.BLUE).count();
            return red <= blue ? Team.RED : Team.BLUE;
        }

        private void startCountdown() {
            state = GameState.COUNTDOWN;
            countdownTaskId = new BukkitRunnable() {
                int count = 10;
                @Override
                public void run() {
                    if (count == 0) {
                        startGame();
                        return;
                    }
                    for (UUID uid : players) {
                        Player p = Bukkit.getPlayer(uid);
                        if (p != null) p.sendMessage(ChatColor.YELLOW + "Game starts in " + count + " seconds");
                    }
                    count--;
                }
            }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        }

        private void cancelCountdown() {
            if (countdownTaskId != -1) {
                Bukkit.getScheduler().cancelTask(countdownTaskId);
                countdownTaskId = -1;
            }
            state = GameState.WAITING;
        }

        private void startGame() {
            state = GameState.PLAYING;
            if (countdownTaskId != -1) {
                Bukkit.getScheduler().cancelTask(countdownTaskId);
                countdownTaskId = -1;
            }
            // distribute spawns
            int i = 0;
            for (UUID uid : players) {
                Player p = Bukkit.getPlayer(uid);
                if (p != null) {
                    Location loc = spawns.get(i % spawns.size());
                    p.teleport(loc);
                    i++;
                }
            }
            // WorldGuard protection
            protectWorld();
            // schedule periodic check
            gameTaskId = new BukkitRunnable() {
                @Override
                public void run() {
                    if (players.size() <= 1 && state == GameState.PLAYING) {
                        endGame();
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L).getTaskId();
        }

        private void protectWorld() {
            Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
            if (wgPlugin instanceof WorldGuardPlugin) {
                protection = new WorldGuardProtection((WorldGuardPlugin) wgPlugin, world);
                protection.protect();
            }
        }

        public void endGame() {
            if (state == GameState.ENDING) return;
            state = GameState.ENDING;
            if (gameTaskId != -1) {
                Bukkit.getScheduler().cancelTask(gameTaskId);
                gameTaskId = -1;
            }
            if (countdownTaskId != -1) {
                Bukkit.getScheduler().cancelTask(countdownTaskId);
                countdownTaskId = -1;
            }
            // teleport players to lobby
            Location lobby = new Location(
                    Bukkit.getWorld(plugin.getConfig().getString("lobby.world")),
                    plugin.getConfig().getDouble("lobby.x"),
                    plugin.getConfig().getDouble("lobby.y"),
                    plugin.getConfig().getDouble("lobby.z"),
                    (float) plugin.getConfig().getDouble("lobby.yaw"),
                    (float) plugin.getConfig().getDouble("lobby.pitch"));
            for (UUID uid : players) {
                Player p = Bukkit.getPlayer(uid);
                if (p != null) {
                    p.teleport(lobby);
                    p.sendMessage(ChatColor.YELLOW + "Game ended");
                }
            }
            players.clear();
            playerTeam.clear();
            lives.clear();
            if (protection != null) protection.unprotect();
            games.remove(name);
        }

        public void onPlayerDeath(Player victim, Player killer) {
            if (state != GameState.PLAYING) return;
            Location loc = victim.getLocation();
            spawnKillEffect(loc);
            int l = lives.getOrDefault(victim.getUniqueId(), 0);
            l--;
            if (l <= 0) {
                victim.setGameMode(GameMode.SPECTATOR);
                players.remove(victim.getUniqueId());
                addStat(victim.getUniqueId(), "deaths", 1);
                if (killer != null) addStat(killer.getUniqueId(), "kills", 1);
            } else {
                lives.put(victim.getUniqueId(), l);
                victim.teleport(spawns.get(0));
                victim.setHealth(20);
            }
            if (players.size() <= 1) endGame();
        }

        public void onBedBreak(Player breaker) {
            // bed break logic: eliminate all players of that team
            // simplified: end game for simplicity
            endGame();
        }

        private void spawnKillEffect(Location loc) {
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 10);
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        }
    }

    // Internal WorldGuard protection wrapper
    private static class WorldGuardProtection {
        private final WorldGuardPlugin wg;
        private final World world;
        private Object region;

        WorldGuardProtection(WorldGuardPlugin wg, World world) {
            this.wg = wg;
            this.world = world;
        }

        void protect() {
            // dummy protection using WorldGuard's RegionManager
            try {
                com.sk89q.worldguard.protection.managers.RegionManager rm = wg.getRegionManager(world);
                com.sk89q.worldguard.protection.regions.ProtectedRegion r = new com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion(
                        "_arena_temp_" + world.getName(),
                        com.sk89q.worldedit.BlockVector3.at(-1000, 0, -1000),
                        com.sk89q.worldedit.BlockVector3.at(1000, 256, 1000));
                rm.addRegion(r);
                region = r;
            } catch (Exception ignored) {}
        }

        void unprotect() {
            if (region != null) {
                try {
                    com.sk89q.worldguard.protection.managers.RegionManager rm = wg.getRegionManager(world);
                    rm.removeRegion("_arena_temp_" + world.getName());
                } catch (Exception ignored) {}
            }
        }
    }
}