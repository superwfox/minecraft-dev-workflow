package com.tahai.qinglong.manager;

import com.tahai.qinglong.model.AnimalData;
import com.tahai.qinglong.model.MapData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ConfigManager {

    private final Map<String, MapData> maps = new LinkedHashMap<>();
    private final Map<String, AnimalData> animals = new LinkedHashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        loadMaps(plugin);
        loadAnimals(plugin);
    }

    public MapData getMapData(String key) {
        return maps.get(key);
    }

    public AnimalData getAnimalData(String key) {
        return animals.get(key);
    }

    private void loadMaps(JavaPlugin plugin) {
        ConfigurationSection mapsSection = plugin.getConfig().getConfigurationSection("maps");
        if (mapsSection == null) return;

        for (String key : mapsSection.getKeys(false)) {
            ConfigurationSection section = mapsSection.getConfigurationSection(key);
            if (section == null) continue;

            String displayName = org.bukkit.ChatColor.translateAlternateColorCodes('&', section.getString("display-name", ""));
            List<String> lore = section.getStringList("lore");
            lore.replaceAll(line -> org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            String world = section.getString("world", "");
            MapData.SpawnPoint spawnPoint = parseSpawnPoint(section.getConfigurationSection("spawn-point"));
            String crossServerCommand = section.getString("cross-server-command", "");

            MapData mapData = new MapData(key, displayName, lore, world, spawnPoint, crossServerCommand);
            maps.put(key, mapData);
        }
    }

    private MapData.SpawnPoint parseSpawnPoint(ConfigurationSection section) {
        if (section == null) return null;
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new MapData.SpawnPoint(x, y, z, yaw, pitch);
    }

    private void loadAnimals(JavaPlugin plugin) {
        ConfigurationSection animalsSection = plugin.getConfig().getConfigurationSection("animals");
        if (animalsSection == null) return;

        for (String key : animalsSection.getKeys(false)) {
            ConfigurationSection section = animalsSection.getConfigurationSection(key);
            if (section == null) continue;

            AnimalData animal = new AnimalData();
            animal.setKey(key);
            animal.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', section.getString("display-name", "")));
            List<String> lore = section.getStringList("lore");
            lore.replaceAll(line -> org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            animal.setLore(lore);
            animal.setScale(section.getDouble("scale", 1.0));

            ConfigurationSection equipSection = section.getConfigurationSection("equipment");
            Map<String, String> equipment = new LinkedHashMap<>();
            if (equipSection != null) {
                for (String slot : equipSection.getKeys(false)) {
                    equipment.put(slot, equipSection.getString(slot));
                }
            }
            animal.setEquipment(equipment);

            List<AnimalData.SkillData> skills = new ArrayList<>();
            ConfigurationSection skillsSection = section.getConfigurationSection("skills");
            if (skillsSection != null) {
                for (String skillKey : skillsSection.getKeys(false)) {
                    ConfigurationSection skillSec = skillsSection.getConfigurationSection(skillKey);
                    if (skillSec == null) continue;

                    AnimalData.SkillData skill = new AnimalData.SkillData();
                    skill.setKey(skillKey);
                    skill.setDisplayName(skillSec.getString("name", ""));
                    skill.setDescription(skillSec.getString("description", ""));
                    skill.setType(skillSec.getString("type", ""));
                    skill.setDamage(skillSec.getDouble("damage", 0.0));
                    skill.setCooldown(skillSec.getInt("cooldown", 0));
                    skill.setRange(skillSec.getDouble("range", 0.0));
                    skills.add(skill);
                }
            }
            animal.setSkills(skills);

            animal.setUnlockLevel(section.getInt("unlock-level", 1));

            List<AnimalData.UpgradeLevel> upgradeLevels = new ArrayList<>();
            ConfigurationSection upgradeSection = section.getConfigurationSection("upgrade-levels");
            if (upgradeSection != null) {
                for (String levelStr : upgradeSection.getKeys(false)) {
                    ConfigurationSection lvlSec = upgradeSection.getConfigurationSection(levelStr);
                    if (lvlSec == null) continue;
                    int level = Integer.parseInt(levelStr);
                    double cost = lvlSec.getDouble("cost", 0.0);
                    upgradeLevels.add(new AnimalData.UpgradeLevel(level, cost, 0.0, 0.0, 0.0));
                }
            }
            animal.setUpgradeLevels(upgradeLevels);

            animals.put(key, animal);
        }
    }
}