package com.tahai.weaponskills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;

public class Main extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private final Map<UUID, String> playerSkills = new HashMap<>();
    private final Map<UUID, Integer> swordCombo = new HashMap<>();
    private final Map<UUID, Boolean> skillActive = new HashMap<>();
    private final Map<UUID, Integer> freezeLevel = new HashMap<>();
    private final List<String> weaponTypes = Arrays.asList("Sword", "Axe", "Trident", "CrossBow", "Mace");
    private File dataFile;

    @Override
    public void onEnable() {
        dataFile = new File(getDataFolder(), "skills.txt");
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        loadSkills();

        if (getCommand("weaponskills") != null) {
            getCommand("weaponskills").setExecutor(this);
            getCommand("weaponskills").setTabCompleter(this);
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveSkills();
        getServer().getScheduler().cancelTasks(this);
    }

    private void loadSkills() {
        playerSkills.clear();
        if (!dataFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    try {
                        UUID uuid = UUID.fromString(parts[0]);
                        String skill = parts[1];
                        if (weaponTypes.contains(skill)) {
                            playerSkills.put(uuid, skill);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (IOException e) {
            getLogger().warning("Could not load skills.txt: " + e.getMessage());
        }
    }

    private void saveSkills() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            for (Map.Entry<UUID, String> entry : playerSkills.entrySet()) {
                writer.write(entry.getKey().toString() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            getLogger().warning("Could not save skills.txt: " + e.getMessage());
        }
    }

    private String getPlayerSkill(Player player) {
        return playerSkills.getOrDefault(player.getUniqueId(), "Sword");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(ChatColor.GRAY + "Usage: /weaponskills <" + String.join("/", weaponTypes) + ">");
            return true;
        }
        String skill = args[0];
        if (!weaponTypes.contains(skill)) {
            player.sendMessage(ChatColor.AQUA + "Invalid skill. Choose from: " + String.join(", ", weaponTypes));
            return true;
        }
        playerSkills.put(player.getUniqueId(), skill);
        saveSkills();
        player.sendMessage(ChatColor.YELLOW + "Skill set to " + skill);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return weaponTypes;
        }
        return Collections.emptyList();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        UUID uid = player.getUniqueId();
        String skill = getPlayerSkill(player);
        ItemStack weapon = player.getInventory().getItemInMainHand();
        Material weaponType = weapon.getType();
        String weaponName = weaponType.name();

        // Sword skill
        if ("Sword".equals(skill) && weaponName.contains("SWORD")) {
            boolean isJumpAttack = !player.isOnGround() && player.getVelocity().getY() < 0;
            if (isJumpAttack) {
                int combo = swordCombo.getOrDefault(uid, 0) + 1;
                swordCombo.put(uid, combo);
                if (combo >= 3 && !skillActive.getOrDefault(uid, false)) {
                    skillActive.put(uid, true);
                    swordCombo.put(uid, 0);
                    player.sendMessage(ChatColor.YELLOW + "技能激活");

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            skillActive.put(uid, false);
                            Player p = Bukkit.getPlayer(uid);
                            if (p != null && p.isOnline()) {
                                p.sendMessage(ChatColor.YELLOW + "技能效果已结束");
                            }
                        }
                    }.runTaskLater(this, 200L);
                }
            }
            if (skillActive.getOrDefault(uid, false)) {
                if (weaponName.contains("NETHERITE")) {
                    event.setDamage(event.getDamage() * 1.5);
                } else if (weaponName.contains("DIAMOND")) {
                    if (event.getEntity() instanceof Player) {
                        Player target = (Player) event.getEntity();
                        int level = freezeLevel.getOrDefault(target.getUniqueId(), 0);
                        level = Math.min(level + 1, 5);
                        freezeLevel.put(target.getUniqueId(), level);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, level - 1));
                    }
                }
            }
        }

        // Axe skill
        if ("Axe".equals(skill) && weaponName.contains("AXE")) {
            event.setDamage(event.getDamage() * 1.5);
            if (event.getEntity() instanceof Player) {
                Player target = (Player) event.getEntity();
                if (target.isBlocking()) {
                    target.setCooldown(Material.SHIELD, 100);
                }
            }
        }

        // Mace skill
        if ("Mace".equals(skill) && weaponName.contains("MACE")) {
            if (player.getVelocity().getY() < 0) {
                double height = Math.abs(player.getVelocity().getY()) * 10;
                event.setDamage(event.getDamage() + height);
            }
            if (weapon.containsEnchantment(org.bukkit.enchantments.Enchantment.WIND_BURST)) {
                event.setDamage(event.getDamage() * 2);
            }
        }

        // Trident skill
        if ("Trident".equals(skill) && weaponName.contains("TRIDENT")) {
            if (player.getWorld().hasStorm() && player.getLocation().getBlock().getBiome().getPrecipitation() == org.bukkit.block.Biome.Precipitation.RAIN) {
                player.getWorld().strikeLightning(event.getEntity().getLocation());
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player)) return;
        Player player = (Player) projectile.getShooter();
        String skill = getPlayerSkill(player);
        if ("CrossBow".equals(skill) && (projectile instanceof Arrow || projectile.getType() == org.bukkit.entity.EntityType.SPECTRAL_ARROW)) {
            projectile.setFireTicks(100);
        }
    }
}