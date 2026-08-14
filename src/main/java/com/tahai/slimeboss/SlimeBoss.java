package com.tahai.slimeboss;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class SlimeBoss extends BukkitRunnable implements Listener {
    private final Plugin plugin;
    private final Location spawnLocation;
    private Slime boss;
    private BossBar bossBar;
    private boolean skillActive = false;
    private long lastUsed = 0L;
    private long lastAiTime = 0L;
    private ArmorStand marker;
    private int skillTick = 0;
    private int phase = 0;
    private int jumpTimer = 0;

    public SlimeBoss(Plugin plugin, Location spawnLocation) {
        this.plugin = plugin;
        this.spawnLocation = spawnLocation.clone();
        this.lastAiTime = System.currentTimeMillis();
        spawnBoss();
        this.bossBar = plugin.getServer().createBossBar("史莱姆王", BarColor.GREEN, BarStyle.SOLID);
    }

    private void spawnBoss() {
        World world = spawnLocation.getWorld();
        Slime slime = (Slime) world.spawn(spawnLocation, Slime.class);
        slime.setSize(8);
        slime.setMaxHealth(600);
        slime.setHealth(600);
        if (slime.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            slime.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15);
        }
        slime.setCustomName("史莱姆王");
        slime.setCustomNameVisible(true);
        slime.setRemoveWhenFarAway(false);
        this.boss = slime;
    }

    @Override
    public void run() {
        if (boss == null || !boss.isValid() || boss.isDead()) {
            destroy();
            return;
        }
        if (skillActive) {
            skillTick();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAiTime < 1000) {
            return;
        }
        lastAiTime = now;
        runAi();
    }

    private void runAi() {
        if (boss == null || !boss.isValid() || boss.isDead()) {
            destroy();
            return;
        }
        refreshBossBar();
        if (skillActive) {
            return;
        }
        if (System.currentTimeMillis() - lastUsed < 15000) {
            return;
        }
        if (ThreadLocalRandom.current().nextDouble() < 0.2) {
            return;
        }
        Player target = findLowestHealthPlayer(28.0);
        if (target == null) {
            return;
        }
        startSkill(target);
    }

    private Player findLowestHealthPlayer(double radius) {
        Player best = null;
        double lowest = Double.MAX_VALUE;
        for (Player p : boss.getWorld().getPlayers()) {
            if (p.isDead() || p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if (boss.getLocation().distance(p.getLocation()) > radius) {
                continue;
            }
            double health = p.getHealth();
            if (health < lowest) {
                lowest = health;
                best = p;
            }
        }
        return best;
    }

    private void refreshBossBar() {
        if (bossBar == null || boss == null || !boss.isValid()) {
            return;
        }
        double max = boss.getMaxHealth();
        double hp = Math.max(0, boss.getHealth());
        bossBar.setProgress(max > 0 ? hp / max : 0.0);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            boolean near = p.getWorld().equals(boss.getWorld()) && p.getLocation().distance(boss.getLocation()) <= 23.0;
            if (near) {
                if (!bossBar.getPlayers().contains(p)) {
                    bossBar.addPlayer(p);
                }
            } else {
                if (bossBar.getPlayers().contains(p)) {
                    bossBar.removePlayer(p);
                }
            }
        }
    }

    private void startSkill(Player target) {
        Location markerLoc = target.getLocation().clone();
        ArmorStand stand = (ArmorStand) markerLoc.getWorld().spawn(markerLoc, ArmorStand.class);
        stand.setVisible(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        stand.setCollidable(false);
        stand.setCanPickupItems(false);
        this.marker = stand;
        this.skillActive = true;
        this.skillTick = 0;
        this.phase = 0;
        this.jumpTimer = 0;
    }

    private void skillTick() {
        if (boss == null || !boss.isValid() || boss.isDead()) {
            endSkill();
            return;
        }
        if (marker == null || !marker.isValid()) {
            endSkill();
            return;
        }
        skillTick++;
        if (skillTick > 200) {
            endSkill();
            return;
        }
        switch (phase) {
            case 0:
                Vector back = boss.getLocation().toVector().subtract(marker.getLocation().toVector());
                if (back.lengthSquared() < 0.001) {
                    back = new Vector(1, 0, 0);
                }
                back.normalize().setY(0.5);
                boss.setVelocity(back);
                phase = 1;
                jumpTimer = 10;
                break;
            case 1:
                if (jumpTimer > 0) {
                    jumpTimer--;
                } else {
                    Vector toMarker = marker.getLocation().toVector().subtract(boss.getLocation().toVector());
                    if (toMarker.lengthSquared() < 0.001) {
                        toMarker = new Vector(0, 0, -1);
                    }
                    toMarker.normalize().multiply(1.5).setY(0.2);
                    boss.setVelocity(toMarker);
                    phase = 2;
                }
                break;
            case 2:
                if (boss.getLocation().distance(marker.getLocation()) <= 3.0) {
                    boss.teleport(marker.getLocation().clone().add(0, 4, 0));
                    boss.setVelocity(new Vector(0, -0.7, 0));
                    phase = 3;
                } else {
                    checkDashCollision();
                }
                break;
            case 3:
                Vector v = boss.getVelocity();
                v.setX(0);
                v.setZ(0);
                boss.setVelocity(v);
                if (boss.isOnGround()) {
                    onLand();
                }
                break;
            default:
                endSkill();
                break;
        }
    }

    private void checkDashCollision() {
        Location bossLoc = boss.getLocation();
        Vector velocity = boss.getVelocity();
        Vector dir = velocity.clone().setY(0);
        if (dir.lengthSquared() < 0.01) {
            dir = bossLoc.getDirection().clone().setY(0);
        }
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(1, 0, 0);
        }
        dir.normalize();
        for (Player p : boss.getWorld().getPlayers()) {
            if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if (p.getLocation().distance(bossLoc) > 2.0) {
                continue;
            }
            Vector toPlayer = p.getLocation().toVector().subtract(bossLoc.toVector()).setY(0);
            if (toPlayer.lengthSquared() > 0.01) {
                if (toPlayer.normalize().dot(dir) < 0.3) {
                    continue;
                }
            }
            if (boss.isValid()) {
                p.damage(25, boss);
            } else {
                p.damage(25);
            }
            Vector knock = p.getLocation().toVector().subtract(bossLoc.toVector()).setY(0);
            if (knock.lengthSquared() < 0.001) {
                knock = new Vector(1, 0, 0);
            }
            knock.normalize().multiply(1.5).setY(0.5);
            p.setVelocity(knock);
        }
    }

    private void onLand() {
        Location loc = boss.getLocation();
        loc.getWorld().spawnParticle(Particle.SLIME, loc, 50, 0.5, 0.5, 0.5, 0.1);
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if (p.getLocation().distance(loc) <= 5.0) {
                if (boss.isValid()) {
                    p.damage(25, boss);
                } else {
                    p.damage(25);
                }
            }
        }
        endSkill();
    }

    private void endSkill() {
        if (marker != null && marker.isValid()) {
            marker.remove();
        }
        marker = null;
        skillActive = false;
        lastUsed = System.currentTimeMillis();
    }

    public void destroy() {
        if (marker != null && marker.isValid()) {
            marker.remove();
        }
        marker = null;
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (boss != null && boss.isValid()) {
            boss.remove();
        }
        boss = null;
        this.cancel();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (boss == null || !event.getEntity().equals(boss)) {
            return;
        }
        event.getDrops().clear();
        event.getDrops().add(new ItemStack(Material.SLIME_BALL, 8));
        event.getDrops().add(new ItemStack(Material.NETHER_STAR, 1));
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(killer);
                head.setItemMeta(meta);
                event.getDrops().add(head);
            }
        }
        destroy();
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
            return;
        }
        World world = spawnLocation.getWorld();
        if (world != null && world.equals(event.getLocation().getWorld())) {
            event.setCancelled(true);
        }
    }
}