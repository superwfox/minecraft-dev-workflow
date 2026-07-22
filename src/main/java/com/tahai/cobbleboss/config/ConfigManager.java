package com.tahai.cobbleboss.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final Plugin plugin;
    private Map<String, BossConfig> bossConfigs;
    private Map<String, String> messages;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.bossConfigs = new HashMap<>();
        this.messages = new HashMap<>();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // 加载 BOSS 配置
        bossConfigs.clear();
        List<Map<?, ?>> bossList = config.getMapList("bosses");
        for (Map<?, ?> entry : bossList) {
            String bossId = (String) entry.get("bossId");
            if (bossId == null) continue;
            BossConfig bc = new BossConfig();
            bc.pokemon = (String) entry.get("pokemon");
            bc.displayName = (String) entry.get("displayName");
            bc.health = toDouble(entry.get("health"));
            bc.attack = toDouble(entry.get("attack"));
            bc.loot = (String) entry.get("loot");
            bc.minIVs = toInt(entry.get("minIVs"));
            bc.aggroRange = toDouble(entry.get("aggroRange"));
            bc.deaggroRange = toDouble(entry.get("deaggroRange"));
            bc.attackType = (String) entry.get("attackType");
            bc.attackRange = toDouble(entry.get("attackRange"));
            bc.speed = toDouble(entry.get("speed"));
            bc.projectile = (String) entry.get("projectile");
            bc.projectileEffects = (Map<String, Object>) entry.get("projectileEffects");
            bc.title = (String) entry.get("title");
            bossConfigs.put(bossId, bc);
        }

        // 加载消息
        messages.clear();
        ConfigurationSection msgSection = config.getConfigurationSection("messages");
        if (msgSection != null) {
            for (String key : msgSection.getKeys(false)) {
                messages.put(key, msgSection.getString(key));
            }
        }
    }

    public BossConfig getBossConfig(String bossId) {
        return bossConfigs.get(bossId);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "");
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return 0.0;
    }

    private int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }

    public static class BossConfig {
        public String pokemon;
        public String displayName;
        public double health;
        public double attack;
        public String loot;
        public int minIVs;
        public double aggroRange;
        public double deaggroRange;
        public String attackType;
        public double attackRange;
        public double speed;
        public String projectile;
        public Map<String, Object> projectileEffects;
        public String title;
    }
}