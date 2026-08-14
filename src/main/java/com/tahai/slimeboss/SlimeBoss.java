package com.tahai.slimeboss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SlimeBoss extends BukkitRunnable implements Listener {

    private static final double MAX_HEALTH = 600;
    private static final double ATTACK_DAMAGE = 15;
    private static final int SKILL_COOLDOWN = 300;
    private static final int SKILL_MAX_TICKS = 300;

    private final Plugin plugin;
    private final BossBar bossBar;
    private Slime boss;
    private ArmorStand marker;
    private final Map<UUID, Long> lastDamageTime = new HashMap<>();
    private final Random random = new Random();

    private boolean skillActive = false;
    private int skillPhase = 0;
    private int skillCooldownLeft = 0;
    private int skillTicks = 0;
    private int skillTriggerDelay = -1;
    private boolean destroyed = false;

    public SlimeBoss(Plugin plugin, Location spawnLoc) {
        this.plugin = plugin;
        this.boss = spawnLoc.getWorld().spawn(spawnLoc, Slime.class);
        this.boss.setSize(8);
        this.boss.setMaxHealth(MAX_HEALTH);
        this.boss.setHealth(MAX_HEALTH);
        this.boss.setPersistent(true);
        this.bossBar = Bukkit.createBossBar("史莱姆王", BarColor.GREEN, BarStyle.SOLID);
    }

    @Override
    public void run() {
        if (boss == null || !boss.isValid() || boss.isDead()) {
            destroy();
            return;
        }
        updateBossBar();
        tickSkill();
    }

    private void updateBossBar() {
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, boss.getHealth() / boss.getMaxHealth())));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(boss.getWorld()) && p.getLocation().distanceSquared(boss.getLocation()) <= 23 * 23) {
                bossBar.addPlayer(p);
            } else {
                bossBar.removePlayer(p);
            }
        }
    }

    private void tickSkill() {
        if (skillActive) {
            skillTicks++;
            if (skillTicks > SKILL_MAX_TICKS) {
                finishSkill();
                return;
            }

            if (skillPhase == 0) {
                if (skillTicks >= 15) {
                    startDash();
                }
            } else {
                if (skillCooldownLeft > 0) {
                    skillCooldownLeft--;
                }

                if (skillPhase == 1) {
                    handleDash();
                } else if (skillPhase == 2) {
                    handleFall();
                }
            }
            return;
        }

        if (skillCooldownLeft > 0) {
            skillCooldownLeft--;
            return;
        }

        if (findTarget() == null) {
            skillTriggerDelay = -1;
            return;
        }

        if (skillTriggerDelay < 0) {
            skillTriggerDelay = 10 + random.nextInt(20);
        }
        skillTriggerDelay--;
        if (skillTriggerDelay > 0) {
            return;
        }

        skillTriggerDelay = -1;
        Player target = findTarget();
        if (target != null) {
            startSkill(target);
        }
    }

    private Player findTarget() {
        List<Player> candidates = new ArrayList<>();
        for (Player p : boss.getWorld().getPlayers()) {
            if (p.isDead() || !p.isOnline()) continue;
            if (p.getLocation().distanceSquared(boss.getLocation()) <= 28 * 28) {
                candidates.add(p);
            }
        }
        return candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
    }

    private void startSkill(Player target) {
        if (boss == null || !boss.isValid() || boss.isDead()) {
            finishSkill();
            return;
        }

        skillActive = true;
        skillPhase = 0;
        skillTicks = 0;
        skillCooldownLeft = 0;

        Location targetLoc = target.getLocation().clone();
        marker = targetLoc.getWorld().spawn(targetLoc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setMarker(true);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setSilent(true);
        });

        if (marker == null) {
            finishSkill();
            return;
        }

        boss.setAI(false);
        Vector dir = boss.getLocation().toVector().subtract(targetLoc.toVector());
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        dir.normalize().setY(0.5);
        boss.setVelocity(dir.multiply(0.7));
    }

    private void startDash() {
        if (marker == null || !marker.isValid() || marker.isDead()) {
            finishSkill();
            return;
        }

        skillPhase = 1;
        skillTicks = 0;
        skillCooldownLeft = SKILL_COOLDOWN;

        Vector dir = marker.getLocation().toVector().subtract(boss.getLocation().toVector());
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0, 0, 1);
        }
        dir.normalize().setY(0.1);
        boss.setVelocity(dir.multiply(1.6));
    }

    private void handleDash() {
        if (marker == null || !marker.isValid() || marker.isDead()) {
            finishSkill();
            return;
        }

        long now = System.currentTimeMillis();
        for (Player p : boss.getWorld().getPlayers()) {
            if (p.isDead() || !p.isOnline()) continue;
            if (p.getLocation().distanceSquared(boss.getLocation()) <= 3 * 3) {
                Long last = lastDamageTime.get(p.getUniqueId());
                if (last == null || now - last >= 1000) {
                    p.damage(ATTACK_DAMAGE, boss);
                    Vector dir = p.getLocation().toVector().subtract(boss.getLocation().toVector());
                    if (dir.lengthSquared() < 0.01) {
                        dir = new Vector(random.nextDouble() - 0.5, 0.5, random.nextDouble() - 0.5);
                    }
                    dir.normalize().setY(0.5);
                    p.setVelocity(dir.multiply(1.5));
                    lastDamageTime.put(p.getUniqueId(), now);
                }
            }
        }

        if (boss.getLocation().distanceSquared(marker.getLocation()) <= 3 * 3 || skillTicks >= 100) {
            boss.teleport(marker.getLocation().clone().add(0, 4, 0));
            boss.setVelocity(new Vector(0, -1.2, 0));
            skillPhase = 2;
            skillTicks = 0;
        }
    }

    private void handleFall() {
        if (boss.isOnGround() || skillTicks >= 120) {
            Location loc = boss.getLocation();
            for (int i = 0; i < 80; i++) {
                loc.getWorld().spawnParticle(Particle.SLIME,
                        loc.clone().add(random.nextDouble() * 6 - 3, random.nextDouble() * 3, random.nextDouble() * 6 - 3),
                        1);
            }

            for (Entity e : loc.getWorld().getNearbyEntities(loc, 6, 6, 6)) {
                if (e instanceof Player p && !p.isDead() && p.isOnline()) {
                    p.damage(ATTACK_DAMAGE, boss);
                }
            }

            finishSkill();
        }
    }

    private void finishSkill() {
        if (boss != null && boss.isValid() && !boss.isDead()) {
            boss.setAI(true);
        }

        skillActive = false;
        skillTicks = 0;
        skillPhase = 0;

        if (marker != null && marker.isValid()) {
            marker.remove();
        }
        marker = null;
    }

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event) {
        if (boss != null && event.getEntity().getUniqueId().equals(boss.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (boss != null && event.getEntity().getUniqueId().equals(boss.getUniqueId())) {
            event.getDrops().add(new ItemStack(Material.SLIME_BLOCK, 64));
            event.getDrops().add(new ItemStack(Material.DIAMOND, 64));
        }
    }

    public void destroy() {
        if (destroyed) return;
        destroyed = true;

        this.cancel();
        bossBar.removeAll();
        lastDamageTime.clear();

        if (marker != null && marker.isValid()) {
            marker.remove();
        }
        marker = null;

        if (boss != null && boss.isValid() && !boss.isDead()) {
            boss.remove();
        }
        boss = null;
    }
}