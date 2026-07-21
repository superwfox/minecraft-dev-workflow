package com.tahai.qinglong.listener;

import com.tahai.qinglong.manager.ConfigManager;
import com.tahai.qinglong.manager.DataManager;
import com.tahai.qinglong.model.AnimalData;
import com.tahai.qinglong.model.MapData;
import com.tahai.qinglong.SpawnPoint;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class CombatListener implements Listener {

    private final DataManager dataManager;
    private final ConfigManager configManager;
    private final Map<UUID, String> currentAnimal = new HashMap<>();

    public CombatListener(DataManager dataManager, ConfigManager configManager) {
        if (dataManager == null || configManager == null) {
            throw new IllegalArgumentException("DataManager and ConfigManager must not be null");
        }
        this.dataManager = dataManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getItem() != null && event.getItem().getType().isBlock()) return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!currentAnimal.containsKey(uuid)) return;
        String animalKey = currentAnimal.get(uuid);
        AnimalData animal = configManager.getAnimalData(animalKey);
        if (animal == null) return;
        List<AnimalData.SkillData> skills = animal.getSkills();
        if (skills.isEmpty()) return;
        AnimalData.SkillData skill = skills.get(0);
        player.sendMessage(ChatColor.YELLOW + "你释放了" + skill.getDisplayName() + ChatColor.YELLOW + "!");
        if (!"蓄力".equals(skill.getType())) {
            // 瞬发技能逻辑
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        UUID uuid = player.getUniqueId();
        if (!currentAnimal.containsKey(uuid)) return;
        String animalKey = currentAnimal.get(uuid);
        AnimalData animal = configManager.getAnimalData(animalKey);
        if (animal == null) return;
        List<AnimalData.SkillData> skills = animal.getSkills();
        if (skills.isEmpty()) return;
        AnimalData.SkillData skill = skills.get(0);
        if ("蓄力".equals(skill.getType())) {
            player.sendMessage(ChatColor.YELLOW + "你蓄力释放了" + skill.getDisplayName() + ChatColor.YELLOW + "!");
        } else {
            player.sendMessage(ChatColor.GRAY + "当前动物没有蓄力技能");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        UUID uuid = damager.getUniqueId();
        if (!currentAnimal.containsKey(uuid)) return;
        String animalKey = currentAnimal.get(uuid);
        DataManager.PlayerData data = dataManager.loadPlayer(damager);
        if (data == null) return;
        AnimalData animal = configManager.getAnimalData(animalKey);
        if (animal == null) return;

        int upgradeLevel = data.getEquipmentUpgrades().getOrDefault(animalKey, 0);
        double damageMultiplier = 1.0;
        for (AnimalData.UpgradeLevel level : animal.getUpgradeLevels()) {
            if (level.getLevel() == upgradeLevel) {
                damageMultiplier += level.getDamage() / 100.0;
                break;
            }
        }
        double originalDamage = event.getDamage();
        event.setDamage(originalDamage * damageMultiplier);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        currentAnimal.remove(victim.getUniqueId());

        // 回城：传送至默认地图重生点
        MapData defaultMap = configManager.getMapData("default");
        if (defaultMap != null) {
            World world = Bukkit.getWorld(defaultMap.getWorld());
            if (world != null) {
                SpawnPoint sp = defaultMap.getSpawnPoint();
                victim.teleport(new Location(world, sp.getX(), sp.getY(), sp.getZ(), sp.getYaw(), sp.getPitch()));
            }
        }

        // 发放经验与青龙币给击杀者
        if (victim.getKiller() instanceof Player) {
            Player killer = victim.getKiller();
            DataManager.PlayerData killerData = dataManager.loadPlayer(killer);
            if (killerData != null) {
                int victimLevel = getPlayerLevel(victim);
                double expAward = 10 + victimLevel * 5;
                int coinAward = 1 + victimLevel / 10;
                killerData.setExp(killerData.getExp() + expAward);
                killerData.setQinglongCoins(killerData.getQinglongCoins() + coinAward);

                // 经验升级检查（占位）
                Plugin plugin = Bukkit.getPluginManager().getPlugin("Qinglong");
                if (plugin instanceof JavaPlugin) {
                    JavaPlugin javaPlugin = (JavaPlugin) plugin;
                    FileConfiguration config = javaPlugin.getConfig();
                    List<?> expFormula = config.getList("experience-formula");
                    if (expFormula != null && !expFormula.isEmpty()) {
                        // 暂时省略具体升级逻辑
                    }
                }

                dataManager.savePlayer(killer);
                killer.sendMessage(ChatColor.YELLOW + "你获得了 " + (int) expAward + " 经验值 和 " + coinAward + " 青龙币");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.loadPlayer(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        dataManager.savePlayer(player);
        currentAnimal.remove(player.getUniqueId());
    }

    private int getPlayerLevel(Player player) {
        DataManager.PlayerData data = dataManager.loadPlayer(player);
        if (data == null) return 1;
        return data.getLevel();
    }
}