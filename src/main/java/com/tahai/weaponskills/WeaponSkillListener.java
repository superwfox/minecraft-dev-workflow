package com.tahai.weaponskills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeaponSkillListener implements Listener {

    private final DataManager dataManager;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public WeaponSkillListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        UUID attackerUUID = attacker.getUniqueId();
        String skill = dataManager.getSkill(attackerUUID);
        if (skill == null) return;

        PlayerData data = playerDataMap.get(attackerUUID);
        if (data == null) {
            data = new PlayerData();
            playerDataMap.put(attackerUUID, data);
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        Material weaponType = weapon.getType();

        // ---- 剑技能 ----
        if (skill.equals("剑") && isSword(weaponType)) {
            handleSwordCombo(event, attacker, data, weaponType);
            if (data.isSwordActive()) {
                handleSwordActive(event, attacker, data, weaponType);
            }
        }

        // ---- 斧技能 ----
        if (skill.equals("斧") && isAxe(weaponType)) {
            handleAxeSkill(event, attacker, data);
        }

        // ---- 三叉戟技能 ----
        if (skill.equals("三叉戟") && weaponType == Material.TRIDENT) {
            handleTridentSkill(event, attacker);
        }

        // ---- 重锤技能 ----
        if (skill.equals("重锤") && weaponType == Material.MACE) {
            handleHammerSkill(event, attacker, data);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter)) return;
        String skill = dataManager.getSkill(shooter.getUniqueId());
        if (skill == null) return;

        // 弩技能：射箭着火
        if (skill.equals("弩") && event.getBow().getType() == Material.CROSSBOW) {
            if (event.getProjectile() instanceof Arrow arrow) {
                arrow.setFireTicks(200);
            }
        }
    }

    private boolean isSword(Material m) {
        return m == Material.WOODEN_SWORD || m == Material.STONE_SWORD || m == Material.IRON_SWORD
                || m == Material.GOLDEN_SWORD || m == Material.DIAMOND_SWORD || m == Material.NETHERITE_SWORD;
    }

    private boolean isAxe(Material m) {
        return m == Material.WOODEN_AXE || m == Material.STONE_AXE || m == Material.IRON_AXE
                || m == Material.GOLDEN_AXE || m == Material.DIAMOND_AXE || m == Material.NETHERITE_AXE;
    }

    private void handleSwordCombo(EntityDamageByEntityEvent event, Player attacker, PlayerData data, Material weapon) {
        Entity target = event.getEntity();
        UUID targetUUID = target.getUniqueId();

        if (data.getComboTarget() != null && !data.getComboTarget().equals(targetUUID)) {
            // 切换目标，重置连击
            data.setComboCount(0);
            data.setComboTarget(targetUUID);
        } else if (data.getComboTarget() == null) {
            data.setComboTarget(targetUUID);
            data.setComboCount(1);
        } else {
            data.setComboCount(data.getComboCount() + 1);
        }

        // 达到3次连击激活技能
        if (data.getComboCount() >= 3 && !data.isSwordActive()) {
            data.setSwordActive(true);
            attacker.sendMessage(org.bukkit.ChatColor.RED + "技能激活");
            // 10秒后取消激活
            new BukkitRunnable() {
                @Override
                public void run() {
                    Player attackerOnline = Bukkit.getPlayer(attacker.getUniqueId());
                    if (attackerOnline != null) {
                        PlayerData pd = playerDataMap.get(attacker.getUniqueId());
                        if (pd != null) {
                            pd.setSwordActive(false);
                            pd.setFrostValue(0);
                            pd.setComboCount(0);
                        }
                    }
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("WeaponSkills"), 200);
        }
    }

    private void handleSwordActive(EntityDamageByEntityEvent event, Player attacker, PlayerData data, Material weapon) {
        // 合金剑强制暴击
        if (weapon == Material.NETHERITE_SWORD) {
            double damage = event.getDamage() * 1.5;
            event.setDamage(damage);
            attacker.getWorld().spawnParticle(Particle.CRIT, attacker.getLocation().add(0, 1, 0), 0);
        }

        // 钻石剑累积霜冻值
        if (weapon == Material.DIAMOND_SWORD) {
            int frost = data.getFrostValue() + 2;
            data.setFrostValue(frost);
            if (frost >= 10) {
                Entity target = event.getEntity();
                if (target instanceof LivingEntity living) {
                    living.setFreezeTicks(200);
                }
                data.setFrostValue(0);
            }
        }
    }

    private void handleAxeSkill(EntityDamageByEntityEvent event, Player attacker, PlayerData data) {
        if (event.getEntity() instanceof Player target) {
            if (target.isBlocking()) {
                int count = data.getAxeShieldBreakCount();
                if (count < 5) {
                    data.setAxeShieldBreakCount(count + 1);
                    if (data.getAxeShieldBreakCount() >= 5) {
                        data.setAxeReady(true);
                        attacker.sendMessage(org.bukkit.ChatColor.RED + "技能已就绪");
                    }
                }
            }
        }

        if (data.isAxeReady()) {
            float attackCooldown = attacker.getCooledAttackStrength(0.5F);
            if (attackCooldown >= 0.9F) {
                event.setDamage(event.getDamage() * 2);
                data.setAxeReady(false);
                data.setAxeShieldBreakCount(0);
                attacker.sendMessage(org.bukkit.ChatColor.RED + "技能激活");
            }
        }
    }

    private void handleTridentSkill(EntityDamageByEntityEvent event, Player attacker) {
        World world = attacker.getWorld();
        if (!world.hasStorm()) return;

        Entity target = event.getEntity();
        world.strikeLightningEffect(target.getLocation());
    }

    private void handleHammerSkill(EntityDamageByEntityEvent event, Player attacker, PlayerData data) {
        if (attacker.getFallDistance() > 5.0) {
            int count = data.getHammerHeavyCount() + 1;
            data.setHammerHeavyCount(count);
            if (count >= 3) {
                data.setHammerReady(true);
                attacker.sendMessage(org.bukkit.ChatColor.RED + "技能已就绪");
            }
        }

        if (data.isHammerReady()) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (weapon.getType() == Material.MACE && weapon.getItemMeta() != null) {
                ItemMeta meta = weapon.getItemMeta();
                Map<Enchantment, Integer> enchants = meta.getEnchants();
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    meta.removeEnchant(entry.getKey());
                    int newLevel = Math.min(entry.getValue() * 2, 255);
                    meta.addEnchant(entry.getKey(), newLevel, true);
                }
                weapon.setItemMeta(meta);
                data.setHammerReady(false);
                data.setHammerHeavyCount(0);
                attacker.sendMessage(org.bukkit.ChatColor.RED + "技能激活");
            }
        }
    }
}