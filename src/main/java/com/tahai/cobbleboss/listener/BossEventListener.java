package com.tahai.cobbleboss.listener;

import com.tahai.cobbleboss.config.ConfigManager;
import com.tahai.cobbleboss.manager.BossManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public class BossEventListener implements Listener {

    private static final NamespacedKey BOSS_KEY = new NamespacedKey("cobbleboss", "boss");
    private static final NamespacedKey BOSS_PROJECTILE_KEY = new NamespacedKey("cobbleboss", "boss_projectile");

    private final BossManager bossManager;
    private final ConfigManager configManager;

    public BossEventListener(BossManager bossManager, ConfigManager configManager) {
        this.bossManager = bossManager;
        this.configManager = configManager;
    }

    private boolean isBoss(Entity entity) {
        return entity.getPersistentDataContainer().has(BOSS_KEY);
    }

    private boolean isBossProjectile(Entity entity) {
        return entity.getPersistentDataContainer().has(BOSS_PROJECTILE_KEY);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        if (!(damaged instanceof LivingEntity)) return;

        // 只处理被伤者是BOSS的情况
        if (!isBoss(damaged)) return;

        Entity damager = event.getDamager();
        Player playerDamager = null;
        if (damager instanceof Player) {
            playerDamager = (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                playerDamager = (Player) projectile.getShooter();
            }
        }

        if (playerDamager != null) {
            bossManager.addDamage(damaged, playerDamager, event.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!isBoss(entity)) return;

        // 取消默认掉落
        event.getDrops().clear();
        event.setDroppedExp(0);

        bossManager.onBossDeath(entity);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!isBossProjectile(projectile)) return;

        Entity hitEntity = event.getHitEntity();
        if (!(hitEntity instanceof LivingEntity)) return;

        // 从弹射物读取配置的bossId
        String bossId = projectile.getPersistentDataContainer().get(
                new NamespacedKey("cobbleboss", "boss_id"),
                org.bukkit.persistence.PersistentDataType.STRING
        );
        if (bossId == null) return;

        // 使用ConfigManager获取boss配置，类型推断避免显式BossConfig类型不匹配
        var bossConfig = configManager.getBossConfig(bossId);
        if (bossConfig == null) return;

        // 应用药水效果
        Map<String, Object> projectileEffects = bossConfig.getProjectileEffects();
        if (projectileEffects != null) {
            List<Map<String, Object>> effectsList = (List<Map<String, Object>>) projectileEffects.get("effects");
            if (effectsList != null) {
                for (Map<String, Object> effectMap : effectsList) {
                    String typeStr = (String) effectMap.get("type");
                    int duration = ((Number) effectMap.getOrDefault("duration", 100)).intValue();
                    int amplifier = ((Number) effectMap.getOrDefault("amplifier", 0)).intValue();
                    boolean ambient = (boolean) effectMap.getOrDefault("ambient", false);
                    boolean particles = (boolean) effectMap.getOrDefault("particles", true);
                    boolean icon = (boolean) effectMap.getOrDefault("icon", true);

                    PotionEffectType potionType = PotionEffectType.getByName(typeStr);
                    if (potionType != null) {
                        ((LivingEntity) hitEntity).addPotionEffect(
                                new PotionEffect(potionType, duration, amplifier, ambient, particles, icon)
                        );
                    }
                }
            }
        }

        // 处理爆炸
        Map<String, Object> explosionConfig = bossConfig.getExplosionConfig();
        if (explosionConfig != null) {
            float power = ((Number) explosionConfig.getOrDefault("power", 0.0)).floatValue();
            boolean setFire = (boolean) explosionConfig.getOrDefault("setFire", false);
            boolean breakBlocks = (boolean) explosionConfig.getOrDefault("breakBlocks", false);
            if (power > 0) {
                Location loc = projectile.getLocation();
                hitEntity.getWorld().createExplosion(loc, power, setFire, breakBlocks);
            }
        }
    }
}