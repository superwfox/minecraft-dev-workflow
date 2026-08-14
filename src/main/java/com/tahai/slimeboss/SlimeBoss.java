package com.tahai.slimeboss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlimeBoss extends BukkitRunnable implements Listener {

    private final Plugin plugin;
    private final Slime boss;
    private final BossBar bar;
    private final Random random = new Random();

    private BukkitTask skillTask;
    private ArmorStand skillArmor;
    private boolean inSkill;
    private int cooldownTicks;

    public SlimeBoss(Location spawn, Plugin plugin) {
        this.plugin = plugin;
        this.boss = spawn.getWorld().spawn(spawn.clone(), Slime.class);
        this.boss.setSize(8);
        this.boss.setMaxHealth(600);
        this.boss.setHealth(600);
        if (this.boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            this.boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15);
        }
        this.boss.setRemoveWhenFarAway(false);
        this.bar = Bukkit.createBossBar("史莱姆王", BarColor.GREEN, BarStyle.SOLID);
    }

    @Override
    public void run() {
        if (!isAlive(boss)) {
            destroy();
            return;
        }

        refreshBossBar();

        if (cooldownTicks > 0) cooldownTicks--;
        if (inSkill) return;
        if (cooldownTicks > 0) return;

        List<Player> candidates = new ArrayList<>();
        for (Player player : boss.getWorld().getPlayers()) {
            if (isAlive(player) && player.getLocation().distance(boss.getLocation()) <= 28.0) {
                candidates.add(player);
            }
        }

        if (!candidates.isEmpty()) {
            startSkill(candidates.get(random.nextInt(candidates.size())));
        }
    }

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event) {
        if (event.getEntity().equals(boss)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!event.getEntity().equals(boss)) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.DIAMOND, 64));
        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.SLIME_BLOCK, 64));
        destroy();
    }

    public void destroy() {
        finishSkill();
        if (bar != null) bar.removeAll();
        if (isAlive(boss)) boss.remove();
        cancel();
    }

    private void refreshBossBar() {
        Location bossLocation = boss.getLocation();

        for (Player player : boss.getWorld().getPlayers()) {
            if (isAlive(player) && player.getLocation().distance(bossLocation) <= 23.0 && !bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            }
        }

        for (Player player : new ArrayList<>(bar.getPlayers())) {
            if (!isAlive(player) || !player.getWorld().equals(boss.getWorld()) || player.getLocation().distance(bossLocation) > 23.0) {
                bar.removePlayer(player);
            }
        }

        if (boss.getMaxHealth() > 0.0) {
            bar.setProgress(Math.max(0.0, Math.min(1.0, boss.getHealth() / boss.getMaxHealth())));
        }
    }

    private void startSkill(Player target) {
        ArmorStand stand = boss.getWorld().spawn(target.getLocation().clone(), ArmorStand.class);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        stand.setRemoveWhenFarAway(false);

        skillArmor = stand;
        inSkill = true;
        cooldownTicks = 400;

        SkillRunnable skill = new SkillRunnable(stand);
        skillTask = skill.runTaskTimer(plugin, 0L, 1L);
    }

    private void finishSkill() {
        if (skillTask != null) {
            skillTask.cancel();
            skillTask = null;
        }
        if (isAlive(skillArmor)) {
            skillArmor.remove();
        }
        skillArmor = null;
        inSkill = false;
    }

    private boolean isAlive(LivingEntity entity) {
        return entity != null && entity.isValid() && !entity.isDead();
    }

    private class SkillRunnable extends BukkitRunnable {

        private static final int MAX_TICK = 200;

        private final ArmorStand stand;
        private int tick;
        private boolean teleported;

        SkillRunnable(ArmorStand stand) {
            this.stand = stand;
        }

        @Override
        public void run() {
            tick++;

            if (tick > MAX_TICK || !isAlive(boss) || !isAlive(stand)) {
                finishSkill();
                return;
            }

            if (tick == 1) {
                Vector jump = boss.getLocation().toVector().subtract(stand.getLocation().toVector());
                if (jump.lengthSquared() < 0.001) {
                    jump.setY(1);
                } else {
                    jump.normalize();
                }
                jump.setY(0.5);
                boss.setVelocity(jump.multiply(0.9));
                return;
            }

            if (!teleported) {
                Vector gap = stand.getLocation().toVector().subtract(boss.getLocation().toVector());

                if (gap.lengthSquared() <= 16.0) {
                    boss.teleport(stand.getLocation().add(0, 4, 0));
                    boss.setVelocity(new Vector(0, -0.5, 0));
                    teleported = true;
                    return;
                }

                if (gap.lengthSquared() > 0.001) {
                    boss.setVelocity(gap.normalize().setY(0).multiply(1.5));
                }

                for (Entity entity : boss.getNearbyEntities(2.0, 2.0, 2.0)) {
                    if (entity instanceof Player player && isAlive(player)) {
                        player.damage(10, boss);

                        Vector knockback = player.getLocation().toVector().subtract(boss.getLocation().toVector());
                        if (knockback.lengthSquared() > 0.001) {
                            knockback.normalize();
                        }
                        knockback.setY(0.5).multiply(1.2);
                        player.setVelocity(knockback);
                    }
                }
            } else {
                boss.setVelocity(new Vector(0, -1.0, 0));

                if (boss.isOnGround()) {
                    boss.getWorld().spawnParticle(Particle.SLIME, boss.getLocation(), 300, 0.6, 0.6, 0.6, 0.1);

                    for (Player player : boss.getWorld().getPlayers()) {
                        if (isAlive(player) && player.getLocation().distance(boss.getLocation()) <= 6.0) {
                            player.damage(25, boss);
                        }
                    }

                    finishSkill();
                }
            }
        }
    }
}