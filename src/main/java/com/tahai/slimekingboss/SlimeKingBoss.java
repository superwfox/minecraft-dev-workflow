package com.tahai.slimekingboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlimeKingBoss extends BukkitRunnable implements Listener {

    private final Location location;
    private final Plugin plugin;
    private Slime boss;
    private final List<Slime> minions = new ArrayList<>();
    private boolean enraged = false;
    private long lastDash = 0L;
    private long lastSplit = 0L;
    private long lastStomp = 0L;
    private final Random random = new Random();
    private BukkitTask dashTask;
    private BukkitTask stompTask;
    private boolean destroyed = false;

    private static final long DASH_COOLDOWN = 5000L;
    private static final long SPLIT_COOLDOWN = 7000L;
    private static final long STOMP_COOLDOWN = 6000L;

    public SlimeKingBoss(Location location) {
        this.location = location;
        Plugin loadedPlugin = Bukkit.getPluginManager().getPlugin("SlimeKingBoss");
        if (loadedPlugin == null) {
            throw new IllegalStateException("SlimeKingBoss plugin not found");
        }
        this.plugin = loadedPlugin;
    }

    public void spawnBoss() {
        World world = location.getWorld();
        if (world == null) return;
        boss = (Slime) world.spawnEntity(location, EntityType.SLIME);
        boss.setSize(8);
        boss.setCustomName(ChatColor.GREEN + "" + ChatColor.BOLD + "史莱姆王");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(600.0D);
        boss.setHealth(600.0D);

        AttributeInstance damage = boss.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(15.0D);
        AttributeInstance speed = boss.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.22D);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void run() {
        if (boss == null || !boss.isValid() || boss.isDead()) {
            destroy();
            return;
        }

        double hp = boss.getHealth();
        if (hp <= 180.0D && !enraged) {
            enraged = true;
            AttributeInstance speed = boss.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(0.34D);
            boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        } else if (hp > 180.0D && enraged) {
            enraged = false;
            AttributeInstance speed = boss.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(0.22D);
            boss.removePotionEffect(PotionEffectType.SPEED);
        }

        long now = System.currentTimeMillis();
        boolean usedGeneralSkill = false;

        if (hp <= 300.0D && now - lastSplit >= SPLIT_COOLDOWN && random.nextDouble() < 0.35D) {
            performSplit();
            lastSplit = now;
            usedGeneralSkill = true;
        }

        if (!usedGeneralSkill) {
            Player dashTarget = findNearestPlayer(30);
            List<String> availableSkills = new ArrayList<>();
            if (now - lastDash >= DASH_COOLDOWN && dashTarget != null) {
                availableSkills.add("dash");
            }
            if (now - lastStomp >= STOMP_COOLDOWN) {
                availableSkills.add("stomp");
            }
            if (!availableSkills.isEmpty()) {
                String chosen = availableSkills.get(random.nextInt(availableSkills.size()));
                if ("dash".equals(chosen)) {
                    if (performDash(dashTarget)) {
                        lastDash = now;
                    }
                } else if ("stomp".equals(chosen)) {
                    performStomp();
                    lastStomp = now;
                }
            }
        }
    }

    private boolean performDash(Player target) {
        if (target == null) return false;
        Vector dir = target.getLocation().toVector().subtract(boss.getLocation().toVector());
        dir.setY(0);
        if (dir.lengthSquared() < 0.0001D) return false;
        dir.normalize().multiply(1.8D);
        dir.setY(0.15D);
        boss.setVelocity(dir);

        if (dashTask != null) {
            dashTask.cancel();
            dashTask = null;
        }
        dashTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                if (boss == null || !boss.isValid() || boss.isDead() || ticks > 40) {
                    if (dashTask != null) {
                        dashTask.cancel();
                        dashTask = null;
                    }
                    return;
                }
                if (boss.getLocation().getBlock().getType().isSolid()) {
                    boss.setVelocity(new Vector(0, 0, 0));
                    if (dashTask != null) {
                        dashTask.cancel();
                        dashTask = null;
                    }
                    return;
                }
                if (target.isOnline() && !target.isDead()
                        && target.getLocation().distanceSquared(boss.getLocation()) < 4.0D) {
                    target.damage(12.0D, boss);
                    Vector kb = target.getLocation().toVector().subtract(boss.getLocation().toVector());
                    kb.setY(0.4D);
                    kb.normalize().multiply(1.5D);
                    target.setVelocity(kb);
                    if (dashTask != null) {
                        dashTask.cancel();
                        dashTask = null;
                    }
                }
            }
        }, 0L, 1L);
        return true;
    }

    private void performSplit() {
        for (int i = 0; i < 3; i++) {
            Slime minion = (Slime) boss.getWorld().spawnEntity(boss.getLocation(), EntityType.SLIME);
            minion.setSize(2);
            minion.setMaxHealth(40.0D);
            minion.setHealth(40.0D);
            minion.setCustomName("史莱姆仆从");
            minion.setCustomNameVisible(true);
            Player nearest = findNearestPlayer(20);
            if (nearest != null) minion.setTarget(nearest);
            minions.add(minion);
        }
    }

    private void performStomp() {
        boss.setVelocity(new Vector(0, 1.2D, 0));
        if (stompTask != null) {
            stompTask.cancel();
            stompTask = null;
        }
        stompTask = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (boss != null && boss.isValid() && !boss.isDead()) {
                    for (Player p : boss.getWorld().getPlayers()) {
                        if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR) continue;
                        if (p.getLocation().distanceSquared(boss.getLocation()) <= 49.0D) {
                            p.damage(8.0D, boss);
                            Vector kb = p.getLocation().toVector().subtract(boss.getLocation().toVector());
                            kb.setY(0.3D);
                            if (kb.lengthSquared() < 0.0001D) {
                                kb = new Vector(0, 0.3D, 0);
                            } else {
                                kb.normalize().multiply(1.2D);
                            }
                            p.setVelocity(kb);
                        }
                    }
                }
                stompTask = null;
            }
        }, 15L);
    }

    public Player findNearestPlayer(int range) {
        if (boss == null) return null;
        Player nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        int rangeSq = range * range;
        for (Player p : boss.getWorld().getPlayers()) {
            if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR || !p.isOnline()) continue;
            double distSq = p.getLocation().distanceSquared(boss.getLocation());
            if (distSq <= rangeSq && distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = p;
            }
        }
        return nearest;
    }

    public void destroy() {
        if (destroyed) return;
        destroyed = true;
        if (dashTask != null) {
            dashTask.cancel();
            dashTask = null;
        }
        if (stompTask != null) {
            stompTask.cancel();
            stompTask = null;
        }
        try {
            this.cancel();
        } catch (IllegalStateException ignored) {}
        for (Slime minion : minions) {
            if (minion != null && minion.isValid() && !minion.isDead()) {
                minion.remove();
            }
        }
        minions.clear();
        HandlerList.unregister(this);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (boss == null || !event.getEntity().equals(boss)) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        World world = boss.getWorld();
        Location loc = boss.getLocation();
        world.dropItemNaturally(loc, new ItemStack(Material.SLIME_BALL, 32));
        world.dropItemNaturally(loc, new ItemStack(Material.SLIME_BLOCK, 8));
        world.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 2));
        destroy();
    }
}