package com.tahai.slimekingboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SlimeKingBoss extends BukkitRunnable implements Listener {

    private final Plugin plugin;
    private final Location spawnLocation;
    private Slime boss;
    private BossBar bossBar;
    private UUID bossUUID;
    private boolean active = true;
    private boolean enraged = false;
    private long lastDashTime = 0;
    private long lastStompTime = 0;
    private long lastCorrosionTime = 0;
    private final Set<UUID> corrosionDamaged = new HashSet<>();
    private final Set<UUID> stompDamaged = new HashSet<>();
    private int corrosiveFieldTicks = 0;

    public SlimeKingBoss(Location location) {
        this.spawnLocation = location;
        this.plugin = Bukkit.getPluginManager().getPlugin("SlimeKingBoss");
        if (this.plugin == null) {
            throw new IllegalStateException("SlimeKingBoss plugin not found");
        }
    }

    @Override
    public void run() {
        if (!active || boss == null || !boss.isValid() || boss.isDead()) {
            destroy();
            return;
        }

        syncBossBar();
        handleEnrage();
        handleCorrosiveField();
        handleDash();
        handleStomp();
    }

    public void spawnBoss() {
        World world = spawnLocation.getWorld();
        if (world == null) return;

        boss = world.spawn(spawnLocation, Slime.class);
        bossUUID = boss.getUniqueId();
        boss.setSize(8);
        boss.setCustomName(ChatColor.GREEN + "" + ChatColor.BOLD + "史莱姆王");
        boss.setCustomNameVisible(true);

        AttributeInstance maxHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) maxHealth.setBaseValue(600);
        boss.setHealth(600);

        AttributeInstance attackDamage = boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamage != null) attackDamage.setBaseValue(15);

        AttributeInstance movementSpeed = boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed != null) movementSpeed.setBaseValue(0.22);

        bossBar = plugin.getServer().createBossBar(
                ChatColor.GREEN + "" + ChatColor.BOLD + "史莱姆王",
                BarColor.GREEN, BarStyle.SOLID);
        bossBar.setProgress(1.0);

        List<Player> players = world.getPlayers();
        for (Player p : players) {
            if (p.getLocation().distance(spawnLocation) <= 30) {
                bossBar.addPlayer(p);
            }
        }
    }

    private void syncBossBar() {
        if (bossBar == null) return;

        AttributeInstance maxHealthAttr = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = (maxHealthAttr != null) ? maxHealthAttr.getValue() : 600.0;
        double progress = maxHealth > 0 ? boss.getHealth() / maxHealth : 0.0;
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

        List<Player> nearby = boss.getWorld().getPlayers();
        for (Player p : nearby) {
            if (p.getLocation().distance(boss.getLocation()) <= 30) {
                bossBar.addPlayer(p);
            }
        }
    }

    private void handleEnrage() {
        if (boss.getHealth() <= 180 && !enraged) {
            enraged = true;
            AttributeInstance speed = boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(0.34);
            boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
        } else if (boss.getHealth() > 180 && enraged) {
            enraged = false;
            AttributeInstance speed = boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(0.22);
        }
    }

    private void handleCorrosiveField() {
        long now = System.currentTimeMillis();
        if (corrosiveFieldTicks > 0) {
            corrosiveFieldTicks--;
            Location center = boss.getLocation();
            World world = boss.getWorld();
            world.spawnParticle(Particle.SLIME, center, 5, 5, 0.5, 5, 0.1);

            if (corrosiveFieldTicks % 4 == 0) {
                for (Entity e : world.getNearbyEntities(center, 5, 5, 5)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        if (corrosionDamaged.add(p.getUniqueId())) {
                            p.damage(3, boss);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false));
                        }
                    }
                }
            }

            if (corrosiveFieldTicks <= 0) {
                lastCorrosionTime = now;
            }
            return;
        }

        if (boss.getHealth() <= 300 && now - lastCorrosionTime >= 7000) {
            if (Math.random() < 0.35) {
                corrosionDamaged.clear();
                corrosiveFieldTicks = 28;
                lastCorrosionTime = now;
            } else {
                lastCorrosionTime = now;
            }
        }
    }

    private void handleDash() {
        long now = System.currentTimeMillis();
        if (now - lastDashTime < 5000) return;

        Player target = findNearestPlayer(30);
        if (target == null) return;

        Location bossLoc = boss.getLocation();
        Location targetLoc = target.getLocation();
        Vector direction = targetLoc.toVector().subtract(bossLoc.toVector()).normalize().setY(0);
        final Vector dashDirection = direction.clone();
        final Vector dashVelocity = direction.multiply(1.8);
        boss.setVelocity(dashVelocity);
        lastDashTime = now;

        final ArmorStand marker = boss.getWorld().spawn(bossLoc, ArmorStand.class);
        marker.setVisible(false);
        marker.setMarker(true);
        marker.setGravity(false);
        marker.setInvulnerable(true);
        marker.setSilent(true);

        new BukkitRunnable() {
            int ticks = 0;
            boolean hit = false;
            final Set<UUID> damaged = new HashSet<>();

            @Override
            public void run() {
                if (!active || !boss.isValid() || boss.isDead() || marker.isDead() || !marker.isValid()) {
                    if (!marker.isDead()) marker.remove();
                    this.cancel();
                    return;
                }

                ticks++;
                if (ticks > 30) {
                    marker.remove();
                    this.cancel();
                    return;
                }

                marker.teleport(boss.getLocation());

                Location ahead = boss.getLocation().add(dashDirection);
                if (!ahead.getBlock().isPassable()) {
                    marker.remove();
                    this.cancel();
                    return;
                }

                Collection<Entity> near = boss.getWorld().getNearbyEntities(marker.getLocation(), 2, 2, 2);
                for (Entity e : near) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        if (!damaged.add(p.getUniqueId())) continue;
                        hit = true;
                        p.damage(12, boss);
                        Vector vel = p.getLocation().toVector().subtract(marker.getLocation().toVector()).normalize().setY(0.8);
                        p.setVelocity(vel);
                    }
                }

                if (hit) {
                    marker.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void handleStomp() {
        long now = System.currentTimeMillis();
        if (now - lastStompTime < 6000) return;

        lastStompTime = now;
        boss.setVelocity(new Vector(0, 1.05, 0));
        stompDamaged.clear();

        boss.getWorld().spawnParticle(Particle.SLIME, boss.getLocation(), 25, 1, 1, 1, 0.2);

        new BukkitRunnable() {
            int airTicks = 0;

            @Override
            public void run() {
                if (!active || boss == null || !boss.isValid() || boss.isDead()) {
                    this.cancel();
                    return;
                }

                if (airTicks > 45) {
                    this.cancel();
                    return;
                }

                airTicks++;
                if (airTicks % 2 == 0) {
                    boss.getWorld().spawnParticle(Particle.SLIME, boss.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
                }

                if (boss.isOnGround() && airTicks > 3) {
                    World world = boss.getWorld();
                    Location loc = boss.getLocation();
                    world.spawnParticle(Particle.SLIME, loc, 60, 7, 1, 7, 0.3);

                    for (Entity e : world.getNearbyEntities(loc, 7, 7, 7)) {
                        if (e instanceof Player) {
                            Player p = (Player) e;
                            if (stompDamaged.add(p.getUniqueId())) {
                                p.damage(8, boss);
                                Vector v = p.getLocation().toVector().subtract(loc.toVector()).normalize();
                                v.setY(1.2);
                                p.setVelocity(v);
                            }
                        }
                    }

                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public Player findNearestPlayer(double radius) {
        Player nearest = null;
        double minDist = radius;
        List<Player> players = boss.getWorld().getPlayers();
        for (Player p : players) {
            double dist = p.getLocation().distance(boss.getLocation());
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getUniqueId().equals(bossUUID)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            event.getDrops().add(new ItemStack(Material.SLIME_BALL, 32));
            event.getDrops().add(new ItemStack(Material.SLIME_BLOCK, 8));
            event.getDrops().add(new ItemStack(Material.NETHER_STAR, 2));
            removeBossInstance();
        } else if (event.getEntity() instanceof Slime) {
            Slime slime = (Slime) event.getEntity();
            if (slime.getSize() > 1 && boss != null && boss.isValid()) {
                event.getDrops().clear();
                event.setDroppedExp(0);
                AttributeInstance maxHealthAttr = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                double maxHealth = (maxHealthAttr != null) ? maxHealthAttr.getValue() : 600.0;
                if (boss.getHealth() < maxHealth) {
                    boss.setHealth(Math.min(maxHealth, boss.getHealth() + 10));
                }
            }
        }
    }

    public void removeBossInstance() {
        destroy();
    }

    public void destroy() {
        active = false;
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (boss != null && boss.isValid()) {
            boss.remove();
        }
        this.cancel();
    }
}