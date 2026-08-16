package com.tahai.slimeboss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SlimeBossAI extends BukkitRunnable implements Listener {

    private static final int DASH_COOLDOWN_TICKS = 200;
    private static final int CAGE_COOLDOWN_TICKS = 400;
    private static final double HEAL_THRESHOLD = 1500.0;
    private static final double TARGET_RANGE = 28.0;
    private static final double BOSS_BAR_RANGE = 60.0;

    private static final int PHASE_IDLE = 0;
    private static final int PHASE_LEAP = 1;
    private static final int PHASE_DASH = 2;
    private static final int PHASE_FALL = 3;
    private static final int PHASE_DONE = 4;

    private final Slime boss;
    private final Plugin plugin;
    private final BossBar bar;
    private final ArmorStand crown;
    private final List<MagmaCube> guards = new ArrayList<>();
    private final Random random = new Random();

    private ArmorStand dashMark;
    private Player dashTarget;
    private int dashPhase;
    private int dashTick;
    private int dashCooldown;
    private int cageCooldown;
    private boolean guardsSpawned;
    private int guardTick;
    private int contactTick;
    private long tick;

    public SlimeBossAI(Slime boss, Plugin plugin) {
        this.boss = boss;
        this.plugin = plugin;
        boss.setAI(false);
        this.crown = spawnCrown();
        this.bar = Bukkit.createBossBar(ChatColor.GREEN + "史莱姆王", BarColor.GREEN, BarStyle.SOLID);
    }

    private ArmorStand spawnCrown() {
        ArmorStand stand = boss.getWorld().spawn(boss.getLocation().add(0, 3.2, 0), ArmorStand.class);
        stand.setMarker(true);
        stand.setVisible(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setInvulnerable(true);
        stand.setPersistent(false);
        stand.getEquipment().setHelmet(new ItemStack(Material.PACKED_ICE));
        return stand;
    }

    private ArmorStand spawnDashMark(Location loc) {
        ArmorStand stand = boss.getWorld().spawn(loc, ArmorStand.class);
        stand.setMarker(true);
        stand.setVisible(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setInvulnerable(true);
        stand.setPersistent(false);
        return stand;
    }

    @Override
    public void run() {
        if (boss == null || !boss.isValid() || boss.isDead()) {
            clean();
            cancel();
            return;
        }
        tick++;
        Location bossLoc = boss.getLocation();

        if (tick % 2 == 0 && crown.isValid()) {
            crown.teleport(bossLoc.clone().add(0, 3.2, 0));
        }

        bar.setProgress(Math.max(0.0, Math.min(1.0, boss.getHealth() / boss.getMaxHealth())));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(boss.getWorld())
                    && p.getLocation().distanceSquared(bossLoc) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
                bar.addPlayer(p);
            } else {
                bar.removePlayer(p);
            }
        }

        List<Player> targets = new ArrayList<>();
        for (Player p : boss.getWorld().getPlayers()) {
            if (p.isValid() && !p.isDead()
                    && p.getLocation().distanceSquared(bossLoc) <= TARGET_RANGE * TARGET_RANGE) {
                targets.add(p);
            }
        }

        if (boss.getHealth() < HEAL_THRESHOLD) {
            if (!guardsSpawned) {
                guardsSpawned = true;
                spawnGuards(targets);
            }
            if (tick % 20 == 0 && boss.getHealth() < boss.getMaxHealth()) {
                boss.setHealth(Math.min(boss.getMaxHealth(), boss.getHealth() + 5));
            }
        }
        updateGuards();

        if (dashCooldown > 0) dashCooldown--;
        if (cageCooldown > 0) cageCooldown--;
        if (!targets.isEmpty()) {
            if (dashCooldown <= 0 && dashPhase == PHASE_IDLE) {
                startDash(targets.get(random.nextInt(targets.size())));
                dashCooldown = DASH_COOLDOWN_TICKS;
            }
            if (cageCooldown <= 0) {
                castCage(targets);
                cageCooldown = CAGE_COOLDOWN_TICKS;
            }
        }
        updateDash();

        contactTick++;
        if (contactTick >= 10) {
            contactTick = 0;
            double range = 2.5 + boss.getSize() * 0.51;
            for (Player p : targets) {
                if (p.getLocation().distanceSquared(bossLoc) <= range * range) {
                    p.damage(25, boss);
                    Vector knock = p.getLocation().toVector().subtract(bossLoc.toVector()).normalize().setY(0.8);
                    p.setVelocity(knock);
                }
            }
        }
    }

    private void spawnGuards(List<Player> targets) {
        for (int i = 0; i < 2; i++) {
            Location loc = boss.getLocation().clone().add(random.nextInt(5) - 2, 0.5, random.nextInt(5) - 2);
            MagmaCube cube = boss.getWorld().spawn(loc, MagmaCube.class);
            cube.setSize(4);
            cube.setCustomName(ChatColor.RED + "岩浆护卫");
            cube.setCustomNameVisible(true);
            cube.setAI(true);
            if (!targets.isEmpty()) {
                cube.setTarget(targets.get(random.nextInt(targets.size())));
            }
            guards.add(cube);
        }
    }

    private void updateGuards() {
        guards.removeIf(g -> g == null || !g.isValid() || g.isDead());
        if (guards.isEmpty()) return;
        guardTick++;
        if (guardTick < 40) return;
        guardTick = 0;
        for (MagmaCube g : guards) {
            Player nearest = null;
            double best = TARGET_RANGE * TARGET_RANGE;
            for (Player p : boss.getWorld().getPlayers()) {
                if (!p.isValid() || p.isDead()) continue;
                double d = p.getLocation().distanceSquared(g.getLocation());
                if (d < best) {
                    best = d;
                    nearest = p;
                }
            }
            if (nearest != null) {
                g.setTarget(nearest);
                Vector dir = nearest.getEyeLocation().toVector().subtract(g.getEyeLocation().toVector());
                if (dir.lengthSquared() > 1e-4) {
                    g.launchProjectile(Fireball.class, dir.normalize().multiply(1.2));
                }
            }
        }
    }

    private void startDash(Player target) {
        this.dashTarget = target;
        Location markLoc = target.getLocation().clone();
        this.dashMark = spawnDashMark(markLoc);
        this.dashPhase = PHASE_LEAP;
        this.dashTick = 0;
        Vector back = boss.getLocation().toVector().subtract(markLoc.toVector());
        if (back.lengthSquared() < 1e-4) {
            back = new Vector(0, 0.8, 0);
        } else {
            back = back.normalize().add(new Vector(0, 0.8, 0));
        }
        boss.setVelocity(back.multiply(1.4));
    }

    private void updateDash() {
        if (dashPhase == PHASE_IDLE) return;
        if (boss.isDead() || !boss.isValid()
                || dashMark == null || !dashMark.isValid()
                || dashTarget == null || !dashTarget.isValid() || dashTarget.isDead() || !dashTarget.isOnline()) {
            endDash();
            return;
        }
        dashTick++;
        Location bossLoc = boss.getLocation();
        switch (dashPhase) {
            case PHASE_LEAP:
                if (dashTick >= 20) {
                    dashPhase = PHASE_DASH;
                    dashTick = 0;
                }
                break;
            case PHASE_DASH: {
                Location markLoc = dashMark.getLocation();
                Vector dir = markLoc.toVector().subtract(bossLoc.toVector());
                if (dir.lengthSquared() > 1e-4) {
                    dir.normalize().setY(0).multiply(1.6);
                    boss.setVelocity(dir);
                }
                if (bossLoc.distanceSquared(markLoc) <= 9 || dashTick >= 60) {
                    boss.teleport(markLoc.clone().add(0, 4, 0));
                    boss.setVelocity(new Vector(0, -1.5, 0));
                    dashPhase = PHASE_FALL;
                    dashTick = 0;
                }
                break;
            }
            case PHASE_FALL:
                boss.setVelocity(new Vector(0, -1.5, 0));
                if (boss.isOnGround()) {
                    shockwave();
                    dashPhase = PHASE_DONE;
                    dashTick = 0;
                } else if (dashTick >= 200) {
                    endDash();
                }
                break;
            case PHASE_DONE:
                endDash();
                break;
            default:
                endDash();
        }
    }

    private void shockwave() {
        Location loc = boss.getLocation();
        World world = loc.getWorld();
        world.spawnParticle(Particle.BLOCK, loc, 250, 3, 1, 3, 0.4, Material.DIRT.createBlockData());
        for (int ring = 1; ring <= 5; ring++) {
            double r = 2.5 + ring * 1.5;
            for (int i = 0; i < 24; i++) {
                double angle = Math.toRadians(i * 15);
                Location p = loc.clone().add(Math.cos(angle) * r, 0.3, Math.sin(angle) * r);
                world.spawnParticle(Particle.BLOCK, p, 6, 0.3, 0.3, 0.3, 0.2, Material.DIRT.createBlockData());
            }
        }
        for (Player p : world.getPlayers()) {
            if (p.isValid() && !p.isDead() && p.getLocation().distanceSquared(loc) <= 64) {
                p.damage(30, boss);
                Vector knock = p.getLocation().toVector().subtract(loc.toVector());
                if (knock.lengthSquared() < 1e-4) knock = new Vector(0, 1, 0);
                knock.normalize().setY(0.9);
                p.setVelocity(knock);
            }
        }
    }

    private void endDash() {
        if (dashMark != null && dashMark.isValid()) dashMark.remove();
        dashMark = null;
        dashTarget = null;
        dashPhase = PHASE_IDLE;
        dashTick = 0;
    }

    private void castCage(List<Player> targets) {
        List<Player> victims = new ArrayList<>(targets);
        Collections.shuffle(victims, random);
        int count = Math.min(3, victims.size());
        for (int i = 0; i < count; i++) {
            victims.get(i).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1, false, true, true));
        }
    }

    @EventHandler
    public void onBossDamaged(EntityDamageEvent event) {
        if (!boss.equals(event.getEntity())) return;
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.ARROW
                || cause == EntityDamageEvent.DamageCause.FALL
                || cause == EntityDamageEvent.DamageCause.MACE_SMASH) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!boss.equals(event.getEntity())) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = star.getItemMeta();
        meta.setDisplayName("史莱姆之星");
        star.setItemMeta(meta);
        event.getDrops().add(star);
        event.getDrops().add(new ItemStack(Material.SLIME_BALL, 16));
        event.getDrops().add(new ItemStack(Material.SLIME_BLOCK, 4));
        cancel();
    }

    public void clean() {
        if (crown != null && crown.isValid()) crown.remove();
        if (dashMark != null && dashMark.isValid()) dashMark.remove();
        dashMark = null;
        dashTarget = null;
        dashPhase = PHASE_IDLE;
        dashTick = 0;
        if (bar != null) bar.removeAll();
        for (MagmaCube g : guards) {
            if (g != null && g.isValid()) g.remove();
        }
        guards.clear();
    }

    @Override
    public void cancel() {
        clean();
        super.cancel();
    }
}