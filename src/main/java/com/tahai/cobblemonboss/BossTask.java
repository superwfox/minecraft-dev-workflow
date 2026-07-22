package com.tahai.cobblemonboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BossTask extends BukkitRunnable {

    private final BossManager bossManager;
    private final Map<String, LivingEntity> bossEntities = new HashMap<>();
    private final Map<UUID, Map<String, Boolean>> playerSeenTitle = new HashMap<>();

    public BossTask(BossManager bossManager) {
        this.bossManager = bossManager;
    }

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CobblemonBoss");
        if (plugin == null) return;

        for (Map.Entry<String, BossTemplate> entry : bossManager.getAllBossTemplates().entrySet()) {
            String bossId = entry.getKey();
            BossTemplate template = entry.getValue();
            BossState state = bossManager.getBossState(bossId);
            if (state == null) continue;

            // 处理存活状态
            if (state.isAlive) {
                LivingEntity entity = bossEntities.get(bossId);
                if (entity == null || entity.isDead() || !entity.isValid()) {
                    // 生成新实体
                    World world = Bukkit.getWorld(template.spawn.world);
                    if (world == null) continue;
                    Location spawnLoc = new Location(world, template.spawn.x, template.spawn.y, template.spawn.z);
                    entity = (LivingEntity) world.spawnEntity(spawnLoc, EntityType.ZOMBIE);
                    entity.setCustomName(template.displayName);
                    entity.setCustomNameVisible(true);
                    entity.setMaxHealth(template.maxHealth);
                    entity.setHealth(state.health > 0 ? state.health : template.maxHealth);
                    entity.setRemoveWhenFarAway(false);
                    bossEntities.put(bossId, entity);
                } else {
                    // 同步生命值（可能被外部修改）
                    state.health = entity.getHealth();
                }

                // 扫描附近玩家
                double aggroRange = 15.0;
                double meleeRange = 2.0;
                Player target = null;
                int highestAggro = -1;

                List<Player> nearbyPlayers = entity.getNearbyEntities(aggroRange, aggroRange, aggroRange)
                        .stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .toList();

                for (Player player : nearbyPlayers) {
                    if (!player.isOnline()) continue;
                    // 首次进入视野发送 title
                    playerSeenTitle.putIfAbsent(player.getUniqueId(), new HashMap<>());
                    if (!playerSeenTitle.get(player.getUniqueId()).getOrDefault(bossId, false)) {
                        player.sendTitle(
                                ChatColor.YELLOW + "⚔ " + template.displayName + " 已降临 ⚔",
                                ChatColor.YELLOW + "准备好战斗！",
                                10, 70, 20
                        );
                        playerSeenTitle.get(player.getUniqueId()).put(bossId, true);
                    }

                    int aggro = state.aggro.getOrDefault(player.getUniqueId(), 0);
                    if (aggro > highestAggro) {
                        highestAggro = aggro;
                        target = player;
                    }
                }

                if (target != null) {
                    // 根据距离选择攻击方式
                    double distance = entity.getLocation().distance(target.getLocation());
                    if (distance <= meleeRange) {
                        // 近战冲锋
                        entity.attack(target);
                        // 增加仇恨
                        int newAggro = state.aggro.getOrDefault(target.getUniqueId(), 0) + (int) template.attack;
                        state.aggro.put(target.getUniqueId(), newAggro);
                    } else if (distance <= aggroRange) {
                        // 远程发射投射物
                        Projectile projectile = entity.launchProjectile(Arrow.class);
                        projectile.setVelocity(target.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(1.5));
                        projectile.setMetadata("bossDamage", new FixedMetadataValue(plugin, template.attack));
                        // 增加少量仇恨
                        int newAggro = state.aggro.getOrDefault(target.getUniqueId(), 0) + 5;
                        state.aggro.put(target.getUniqueId(), newAggro);
                    }
                } else {
                    // 没有玩家在范围内，缓慢回血
                    if (state.health < template.maxHealth) {
                        double heal = template.maxHealth * 0.02; // 每秒回复2%最大生命
                        state.health = Math.min(template.maxHealth, state.health + heal);
                        entity.setHealth(state.health);
                    }
                }

                // 更新状态
                bossManager.updateHealth(bossId, state.health);
                bossManager.updateAggro(bossId, target != null ? target.getUniqueId() : null, highestAggro);
            } else {
                // 死亡状态：减少重生倒计时
                if (state.remainingRespawnTime > 0) {
                    state.remainingRespawnTime--;
                    if (state.remainingRespawnTime <= 0) {
                        // 重生
                        state.isAlive = true;
                        state.health = template.maxHealth;
                        state.aggro.clear();
                        state.remainingRespawnTime = 0;
                        bossManager.setAlive(bossId, true);
                        bossManager.updateHealth(bossId, template.maxHealth);
                        // 清除实体引用，下一 tick 会重新生成
                        LivingEntity oldEntity = bossEntities.remove(bossId);
                        if (oldEntity != null) oldEntity.remove();
                        // 清除玩家看到记录（可选）
                    }
                }
            }

            // 清空已经离线的玩家仇恨记录
            state.aggro.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null || !Bukkit.getPlayer(uuid).isOnline());
        }
    }
}