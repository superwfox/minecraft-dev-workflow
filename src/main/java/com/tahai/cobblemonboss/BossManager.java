package com.tahai.cobblemonboss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BossManager {

    private final Plugin plugin;
    private final Map<String, BossTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, BossState> states = new ConcurrentHashMap<>();

    private final File bossesFile;
    private final File statesFile;

    public BossManager(Plugin plugin) {
        this.plugin = plugin;
        bossesFile = new File(plugin.getDataFolder(), "bosses.yml");
        statesFile = new File(plugin.getDataFolder(), "bosses_states.yml");
        loadAll();
    }

    private void loadAll() {
        // Load templates from bosses.yml
        if (!bossesFile.exists()) {
            plugin.saveResource("bosses.yml", false);
        }
        YamlConfiguration bossConfig = YamlConfiguration.loadConfiguration(bossesFile);
        for (String id : bossConfig.getKeys(false)) {
            ConfigurationSection sec = bossConfig.getConfigurationSection(id);
            if (sec == null) continue;

            String displayName = sec.getString("displayName", id);
            double maxHealth = sec.getDouble("maxHealth", 100);
            double attack = sec.getDouble("attack", 10);
            Location spawnLocation = null;
            if (sec.contains("spawn")) {
                ConfigurationSection spawnSec = sec.getConfigurationSection("spawn");
                if (spawnSec != null) {
                    String worldName = spawnSec.getString("world");
                    World world = worldName != null ? Bukkit.getWorld(worldName) : null;
                    if (world != null) {
                        double x = spawnSec.getDouble("x");
                        double y = spawnSec.getDouble("y");
                        double z = spawnSec.getDouble("z");
                        spawnLocation = new Location(world, x, y, z);
                    }
                }
            }
            List<String> rewards = sec.getStringList("rewards");
            BossTemplate template = new BossTemplate(id, displayName, maxHealth, attack, spawnLocation, rewards);
            templates.put(id, template);
        }

        // Load states from bosses_states.yml
        if (statesFile.exists()) {
            YamlConfiguration stateConfig = YamlConfiguration.loadConfiguration(statesFile);
            for (String id : stateConfig.getKeys(false)) {
                ConfigurationSection sec = stateConfig.getConfigurationSection(id);
                if (sec == null) continue;

                boolean isAlive = sec.getBoolean("isAlive", true);
                long remainingRespawnTime = sec.getLong("remainingRespawnTime", 0);
                double health = sec.getDouble("health", 0);
                Map<UUID, Integer> aggro = new HashMap<>();
                if (sec.contains("aggro")) {
                    ConfigurationSection aggroSec = sec.getConfigurationSection("aggro");
                    if (aggroSec != null) {
                        for (String key : aggroSec.getKeys(false)) {
                            try {
                                UUID uuid = UUID.fromString(key);
                                int value = aggroSec.getInt(key);
                                aggro.put(uuid, value);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
                BossState state = new BossState(isAlive, remainingRespawnTime, health, aggro);
                states.put(id, state);
            }
        }
    }

    public BossTemplate getBossTemplate(String id) {
        return templates.get(id);
    }

    public Map<String, BossTemplate> getAllBossTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    public BossState getBossState(String id) {
        return states.computeIfAbsent(id, k -> new BossState(true, 0, 0, new HashMap<>()));
    }

    public void setSpawnPoint(String bossId, Location location) {
        BossTemplate template = templates.get(bossId);
        if (template == null) return;
        template.spawnLocation = location;
        saveBossesConfig();
    }

    public void updateHealth(String bossId, double health) {
        BossState state = getBossState(bossId);
        state.health = health;
        saveStatesConfig();
    }

    public void updateAggro(String bossId, UUID player, int aggro) {
        BossState state = getBossState(bossId);
        state.aggro.put(player, aggro);
        saveStatesConfig();
    }

    public void setAlive(String bossId, boolean alive) {
        BossState state = getBossState(bossId);
        state.isAlive = alive;
        if (!alive) {
            state.remainingRespawnTime = System.currentTimeMillis() + 600000; // 10 minutes default
        }
        saveStatesConfig();
    }

    public void save() {
        saveBossesConfig();
        saveStatesConfig();
    }

    public void shutdown() {
        save();
    }

    private void saveBossesConfig() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, BossTemplate> entry : templates.entrySet()) {
            String id = entry.getKey();
            BossTemplate t = entry.getValue();
            config.set(id + ".displayName", t.displayName);
            config.set(id + ".maxHealth", t.maxHealth);
            config.set(id + ".attack", t.attack);
            if (t.spawnLocation != null) {
                config.set(id + ".spawn.world", t.spawnLocation.getWorld().getName());
                config.set(id + ".spawn.x", t.spawnLocation.getX());
                config.set(id + ".spawn.y", t.spawnLocation.getY());
                config.set(id + ".spawn.z", t.spawnLocation.getZ());
            }
            config.set(id + ".rewards", t.rewards);
        }
        try {
            config.save(bossesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save bosses.yml: " + e.getMessage());
        }
    }

    private void saveStatesConfig() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, BossState> entry : states.entrySet()) {
            String id = entry.getKey();
            BossState s = entry.getValue();
            config.set(id + ".isAlive", s.isAlive);
            config.set(id + ".remainingRespawnTime", s.remainingRespawnTime);
            config.set(id + ".health", s.health);
            if (!s.aggro.isEmpty()) {
                for (Map.Entry<UUID, Integer> a : s.aggro.entrySet()) {
                    config.set(id + ".aggro." + a.getKey().toString(), a.getValue());
                }
            }
        }
        try {
            config.save(statesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save bosses_states.yml: " + e.getMessage());
        }
    }

    // Inner classes for data

    public static class BossTemplate {
        public final String id;
        public final String displayName;
        public final double maxHealth;
        public final double attack;
        public Location spawnLocation;
        public final List<String> rewards;

        public BossTemplate(String id, String displayName, double maxHealth, double attack, Location spawnLocation, List<String> rewards) {
            this.id = id;
            this.displayName = displayName;
            this.maxHealth = maxHealth;
            this.attack = attack;
            this.spawnLocation = spawnLocation;
            this.rewards = rewards != null ? rewards : new ArrayList<>();
        }
    }

    public static class BossState {
        public boolean isAlive;
        public long remainingRespawnTime;
        public double health;
        public final Map<UUID, Integer> aggro;

        public BossState(boolean isAlive, long remainingRespawnTime, double health, Map<UUID, Integer> aggro) {
            this.isAlive = isAlive;
            this.remainingRespawnTime = remainingRespawnTime;
            this.health = health;
            this.aggro = aggro != null ? aggro : new HashMap<>();
        }
    }
}