package com.tahai.cobbleboss.manager;

import com.tahai.cobbleboss.config.ConfigManager;
import com.tahai.cobbleboss.model.BossInstance;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.*;

public class BossManager {

    private final Plugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BossInstance> activeBosses = new HashMap<>();
    private final Map<UUID, String> bossIdMap = new HashMap<>();

    public BossManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void spawnBoss(String bossId, Location location) {
        Object bossConfig = configManager.getBossConfig(bossId);
        if (bossConfig == null) return;

        String pokemonId = null;
        String displayName = null;
        double health = 0;
        double attack = 0;
        String attackType = "melee";
        double attackRange = 2.0;
        double speed = 1.0;
        Map<String, Object> projectileEffects = new HashMap<>();
        Map<String, Object> explosionConfig = new HashMap<>();

        LivingEntity pokemonEntity = location.getWorld().spawn(location, Zombie.class);
        pokemonEntity.setAI(false);

        BossInstance bossInstance = new BossInstance(
                pokemonEntity.getUniqueId(),
                health,
                attack,
                attackType,
                attackRange,
                speed,
                projectileEffects,
                explosionConfig
        );

        activeBosses.put(bossInstance.getEntityUuid(), bossInstance);
        bossIdMap.put(bossInstance.getEntityUuid(), bossId);
    }

    public void tick() {
        Map<UUID, BossInstance> copy = new HashMap<>(activeBosses);

        for (Map.Entry<UUID, BossInstance> entry : copy.entrySet()) {
            BossInstance instance = entry.getValue();
            Entity entity = Bukkit.getEntity(instance.getEntityUuid());
            if (entity == null || !entity.isValid()) {
                onBossDeath(entity);
                continue;
            }
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            double aggroRange = 10.0;
            double deaggroRange = 15.0;

            Player nearest = null;
            for (Player player : entity.getWorld().getPlayers()) {
                double dist = entity.getLocation().distance(player.getLocation());
                if (nearest == null || dist < entity.getLocation().distance(nearest.getLocation())) {
                    nearest = player;
                }
            }

            if (nearest != null) {
                double dist = entity.getLocation().distance(nearest.getLocation());
                if (!instance.isFighting() && dist <= aggroRange) {
                    instance.setFighting(true);
                    String titleMsg = configManager.getMessage("enterBattle");
                    if (titleMsg != null && !titleMsg.isEmpty()) {
                        nearest.sendTitle(
                                ChatColor.translateAlternateColorCodes('&', titleMsg),
                                "",
                                10, 40, 10
                        );
                    }
                } else if (instance.isFighting() && dist > deaggroRange) {
                    instance.setFighting(false);
                }
            }

            int cooldown = instance.getAttackCooldown();
            cooldown--;
            if (cooldown <= 0 && instance.isFighting()) {
                cooldown = 20;
                UUID targetUuid = null;
                if (!instance.getDamageMap().isEmpty()) {
                    targetUuid = Collections.max(instance.getDamageMap().entrySet(), Map.Entry.comparingByValue()).getKey();
                }
                Player target = targetUuid != null ? Bukkit.getPlayer(targetUuid) : nearest;
                if (target != null && target.isOnline()) {
                    String attackType = instance.getAttackType();
                    if ("melee".equalsIgnoreCase(attackType)) {
                        Vector direction = target.getLocation().toVector()
                                .subtract(entity.getLocation().toVector()).normalize();
                        entity.setVelocity(direction.multiply(instance.getSpeed()));
                        if (entity.getLocation().distance(target.getLocation()) <= instance.getAttackRange()) {
                            target.damage(instance.getAttack(), entity);
                        }
                    } else if ("ranged".equalsIgnoreCase(attackType)) {
                        Snowball projectile = entity.getWorld().spawn(
                                entity.getLocation().add(0, 1, 0), Snowball.class
                        );
                        Vector dir = target.getLocation().toVector()
                                .subtract(entity.getLocation().toVector()).normalize();
                        projectile.setVelocity(dir.multiply(2.0));
                        NamespacedKey key = new NamespacedKey(plugin, "bossUuid");
                        projectile.getPersistentDataContainer().set(
                                key, PersistentDataType.STRING, entity.getUniqueId().toString()
                        );
                    }
                }
                instance.setAttackCooldown(cooldown);
            } else {
                instance.setAttackCooldown(cooldown);
            }

            if (!instance.isFighting()) {
                double currentHealth = livingEntity.getHealth();
                double maxHealth = livingEntity.getMaxHealth();
                if (currentHealth < maxHealth) {
                    double newHealth = Math.min(currentHealth + 0.5, maxHealth);
                    livingEntity.setHealth(newHealth);
                    instance.setHealth(newHealth);
                }
            }
        }
    }

    public void addDamage(Entity bossEntity, Player damager, double damage) {
        BossInstance instance = activeBosses.get(bossEntity.getUniqueId());
        if (instance == null) return;
        Map<UUID, Double> damageMap = instance.getDamageMap();
        damageMap.merge(damager.getUniqueId(), damage, Double::sum);
        instance.setDamageMap(damageMap);
    }

    public void onBossDeath(Entity bossEntity) {
        if (bossEntity == null) return;
        BossInstance instance = activeBosses.remove(bossEntity.getUniqueId());
        if (instance == null) return;

        String bossId = bossIdMap.remove(bossEntity.getUniqueId());
        Object bossConfig = configManager.getBossConfig(bossId);
        if (bossConfig == null) return;

        Map<UUID, Double> damageMap = instance.getDamageMap();
        double totalDamage = damageMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalDamage <= 0) return;

        bossEntity.remove();
        activeBosses.remove(bossEntity.getUniqueId());
    }

    public void updateBossesFromConfig() {
    }
}