package com.tahai.cobbleboss.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossInstance {
    private UUID entityUuid;
    private double health;
    private double attack;
    private String attackType;
    private double attackRange;
    private double speed;
    private Map<String, Object> projectileEffects;
    private Map<String, Object> explosionConfig;
    private Map<UUID, Double> damageMap;
    private boolean isFighting;
    private int attackCooldown;
    private boolean alive;

    public BossInstance() {
        this.damageMap = new HashMap<>();
        this.isFighting = false;
        this.alive = true;
        this.attackCooldown = 0;
    }

    public BossInstance(UUID entityUuid, double health, double attack, String attackType,
                        double attackRange, double speed, Map<String, Object> projectileEffects,
                        Map<String, Object> explosionConfig) {
        this.entityUuid = entityUuid;
        this.health = health;
        this.attack = attack;
        this.attackType = attackType;
        this.attackRange = attackRange;
        this.speed = speed;
        this.projectileEffects = projectileEffects != null ? projectileEffects : new HashMap<>();
        this.explosionConfig = explosionConfig != null ? explosionConfig : new HashMap<>();
        this.damageMap = new HashMap<>();
        this.isFighting = false;
        this.alive = true;
        this.attackCooldown = 0;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public void setEntityUuid(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getAttack() {
        return attack;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }

    public String getAttackType() {
        return attackType;
    }

    public void setAttackType(String attackType) {
        this.attackType = attackType;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Map<String, Object> getProjectileEffects() {
        return projectileEffects;
    }

    public void setProjectileEffects(Map<String, Object> projectileEffects) {
        this.projectileEffects = projectileEffects;
    }

    public Map<String, Object> getExplosionConfig() {
        return explosionConfig;
    }

    public void setExplosionConfig(Map<String, Object> explosionConfig) {
        this.explosionConfig = explosionConfig;
    }

    public Map<UUID, Double> getDamageMap() {
        return damageMap;
    }

    public void setDamageMap(Map<UUID, Double> damageMap) {
        this.damageMap = damageMap;
    }

    public boolean isFighting() {
        return isFighting;
    }

    public void setFighting(boolean fighting) {
        isFighting = fighting;
    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}