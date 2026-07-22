package com.tahai.cobbleboss;

import java.util.List;
import java.util.Map;

public class BossConfig {
    private final String bossId;
    private final String pokemon;
    private final String displayName;
    private final double health;
    private final double attack;
    private final List<String> loot;
    private final int minIVs;
    private final int aggroRange;
    private final int deaggroRange;
    private final String attackType;
    private final double attackRange;
    private final double speed;
    private final String projectile;
    private final Map<String, Object> projectileEffects;
    private final Map<String, Object> explosionConfig;
    private final String title;

    public BossConfig(String bossId, String pokemon, String displayName,
                      double health, double attack, List<String> loot,
                      int minIVs, int aggroRange, int deaggroRange,
                      String attackType, double attackRange, double speed,
                      String projectile, Map<String, Object> projectileEffects,
                      Map<String, Object> explosionConfig, String title) {
        this.bossId = bossId;
        this.pokemon = pokemon;
        this.displayName = displayName;
        this.health = health;
        this.attack = attack;
        this.loot = loot;
        this.minIVs = minIVs;
        this.aggroRange = aggroRange;
        this.deaggroRange = deaggroRange;
        this.attackType = attackType;
        this.attackRange = attackRange;
        this.speed = speed;
        this.projectile = projectile;
        this.projectileEffects = projectileEffects;
        this.explosionConfig = explosionConfig;
        this.title = title;
    }

    public String getBossId() { return bossId; }
    public String getPokemon() { return pokemon; }
    public String getDisplayName() { return displayName; }
    public double getHealth() { return health; }
    public double getAttack() { return attack; }
    public List<String> getLoot() { return loot; }
    public int getMinIVs() { return minIVs; }
    public int getAggroRange() { return aggroRange; }
    public int getDeaggroRange() { return deaggroRange; }
    public String getAttackType() { return attackType; }
    public double getAttackRange() { return attackRange; }
    public double getSpeed() { return speed; }
    public String getProjectile() { return projectile; }
    public Map<String, Object> getProjectileEffects() { return projectileEffects; }
    public Map<String, Object> getExplosionConfig() { return explosionConfig; }
    public String getTitle() { return title; }
}