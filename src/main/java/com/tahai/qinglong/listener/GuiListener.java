package com.tahai.qinglong.listener;

import com.tahai.qinglong.PlayerData;
import com.tahai.qinglong.gui.MenuGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GuiListener implements Listener {

    private final Plugin plugin;
    private final Map<UUID, String> selectedMap = new HashMap<>();

    public GuiListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuGUI)) return;
        event.setCancelled(true);

        MenuGUI gui = (MenuGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= gui.getInventory().getSize()) return;

        ItemStack item = gui.getInventory().getItem(slot);
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        String displayName = meta.getDisplayName();
        List<String> lore = meta.getLore();

        String menuType = gui.getMenuType();
        if (menuType == null) return;

        switch (menuType) {
            case "MAIN":
                handleMainClick(player, gui, displayName);
                break;
            case "MAPS":
                handleMapClick(player, gui, displayName, lore, item);
                break;
            case "ANIMALS":
                handleAnimalClick(player, gui, displayName, lore, item);
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        selectedMap.remove(player.getUniqueId());
    }

    private void handleMainClick(Player player, MenuGUI gui, String displayName) {
        if (displayName.contains("地图传送") || displayName.contains("地图")) {
            gui.openMapSelection(player);
        } else if (displayName.contains("动物选择") || displayName.contains("动物")) {
            gui.openAnimalSelection(player);
        }
    }

    private void handleMapClick(Player player, MenuGUI gui, String displayName, List<String> lore, ItemStack item) {
        String mapKey = null;
        if (lore != null && !lore.isEmpty()) {
            for (String line : lore) {
                String trimmed = ChatColor.stripColor(line).trim();
                if (trimmed.startsWith("§") || trimmed.isEmpty()) continue;
                // 尝试直接用显示名称或 lore 中的标识
                mapKey = trimmed;
                break;
            }
        }
        if (mapKey == null) {
            mapKey = ChatColor.stripColor(displayName).replaceAll("[^a-zA-Z0-9_]", "");
        }
        if (mapKey == null || mapKey.isEmpty()) return;

        ConfigurationSection mapsSection = plugin.getConfig().getConfigurationSection("maps");
        if (mapsSection == null) return;
        for (String key : mapsSection.getKeys(false)) {
            ConfigurationSection mapSection = mapsSection.getConfigurationSection(key);
            if (mapSection == null) continue;
            String mapDisplayName = mapSection.getString("display-name", "");
            if (ChatColor.stripColor(mapDisplayName).equalsIgnoreCase(ChatColor.stripColor(displayName))) {
                mapKey = key;
                break;
            }
        }

        ConfigurationSection mapSection = mapsSection.getConfigurationSection(mapKey);
        if (mapSection == null) {
            player.sendMessage(ChatColor.AQUA + "未知地图");
            return;
        }

        String worldName = mapSection.getString("world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.AQUA + "世界未加载");
            return;
        }

        ConfigurationSection spawnSection = mapSection.getConfigurationSection("spawn-point");
        if (spawnSection == null) return;

        double x = spawnSection.getDouble("x");
        double y = spawnSection.getDouble("y");
        double z = spawnSection.getDouble("z");
        float yaw = (float) spawnSection.getDouble("yaw");
        float pitch = (float) spawnSection.getDouble("pitch");
        Location loc = new Location(world, x, y, z, yaw, pitch);

        String crossServerCommand = mapSection.getString("cross-server-command", "");
        if (!crossServerCommand.isEmpty()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), crossServerCommand.replace("%player%", player.getName()));
            return;
        }

        // 记录选定地图
        selectedMap.put(player.getUniqueId(), mapKey);

        // 传送
        player.teleport(loc);

        // 设置出生点 (respawn location)
        player.setBedSpawnLocation(loc, true);

        player.sendMessage(ChatColor.YELLOW + "已传送到 " + ChatColor.stripColor(mapSection.getString("display-name", mapKey)));
        player.closeInventory();
    }

    private void handleAnimalClick(Player player, MenuGUI gui, String displayName, List<String> lore, ItemStack item) {
        String animalKey = null;
        if (lore != null && !lore.isEmpty()) {
            for (String line : lore) {
                String trimmed = ChatColor.stripColor(line).trim();
                if (!trimmed.isEmpty()) {
                    animalKey = trimmed;
                    break;
                }
            }
        }
        if (animalKey == null) {
            animalKey = ChatColor.stripColor(displayName).replaceAll("[^a-zA-Z0-9_]", "");
        }

        ConfigurationSection animalsSection = plugin.getConfig().getConfigurationSection("animals");
        if (animalsSection == null) return;

        ConfigurationSection animalSection = null;
        String foundKey = null;
        for (String key : animalsSection.getKeys(false)) {
            ConfigurationSection sec = animalsSection.getConfigurationSection(key);
            if (sec == null) continue;
            String secDisplay = sec.getString("display-name", "");
            if (ChatColor.stripColor(secDisplay).equalsIgnoreCase(ChatColor.stripColor(displayName))) {
                animalSection = sec;
                foundKey = key;
                break;
            }
        }
        if (animalSection == null) {
            player.sendMessage(ChatColor.AQUA + "未知动物");
            return;
        }

        // 加载玩家数据
        PlayerData data = loadPlayerData(player);
        Set<String> unlocked = data.getUnlockedAnimals();
        int currentLevel = data.getLevel();
        int qinglongCoins = data.getQinglongCoins();

        int unlockLevel = animalSection.getInt("unlock-level", 1);
        if (!unlocked.contains(foundKey)) {
            if (currentLevel < unlockLevel) {
                player.sendMessage(ChatColor.AQUA + "需要等级 " + unlockLevel + " 才能解锁此动物");
                return;
            }
            // 检查青龙币（假设解锁需要一定数量，此处取 upgrade-levels 第一个的 cost 或固定值 100）
            int cost = 100;
            ConfigurationSection upgrades = animalSection.getConfigurationSection("upgrade-levels");
            if (upgrades != null && upgrades.getKeys(false).size() > 0) {
                String first = upgrades.getKeys(false).iterator().next();
                cost = (int) upgrades.getDouble(first + ".cost", 100);
            }
            if (qinglongCoins < cost) {
                player.sendMessage(ChatColor.AQUA + "需要 " + cost + " 个青龙币");
                return;
            }
            // 扣除青龙币并解锁
            data.setQinglongCoins(qinglongCoins - cost);
            unlocked.add(foundKey);
            data.setUnlockedAnimals(unlocked);
            savePlayerData(player, data);
            player.sendMessage(ChatColor.YELLOW + "成功解锁动物 " + ChatColor.stripColor(animalSection.getString("display-name", foundKey)));
        }

        // 变身逻辑（简化：发送消息，未来可扩展）
        player.sendMessage(ChatColor.YELLOW + "变身 " + ChatColor.stripColor(animalSection.getString("display-name", foundKey)));

        // 传送至地图随机位置
        String mapKey = selectedMap.get(player.getUniqueId());
        if (mapKey == null) {
            // 如果没有选中的地图，使用默认地图
            ConfigurationSection mapsSection = plugin.getConfig().getConfigurationSection("maps");
            if (mapsSection != null) {
                for (String key : mapsSection.getKeys(false)) {
                    mapKey = key;
                    break;
                }
            }
            if (mapKey == null) {
                player.sendMessage(ChatColor.AQUA + "没有可用的地图");
                return;
            }
        }
        ConfigurationSection mapSection = plugin.getConfig().getConfigurationSection("maps." + mapKey);
        if (mapSection == null) return;
        String worldName = mapSection.getString("world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        // 生成地图内随机位置（以 spawn-point 为中心 50 格半径）
        ConfigurationSection spawnSection = mapSection.getConfigurationSection("spawn-point");
        if (spawnSection == null) return;
        double baseX = spawnSection.getDouble("x");
        double baseZ = spawnSection.getDouble("z");
        double baseY = spawnSection.getDouble("y");
        Random rand = new Random();
        double rx = baseX + (rand.nextDouble() - 0.5) * 100;
        double rz = baseZ + (rand.nextDouble() - 0.5) * 100;
        Location loc = new Location(world, rx, baseY, rz);
        player.teleport(loc);

        player.closeInventory();
    }

    private PlayerData loadPlayerData(Player player) {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            return new PlayerData();
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        String uuid = player.getUniqueId().toString();
        ConfigurationSection playerSection = cfg.getConfigurationSection(uuid);
        if (playerSection == null) {
            return new PlayerData();
        }
        PlayerData data = new PlayerData();
        data.setLevel(playerSection.getInt("level", 1));
        data.setExp(playerSection.getDouble("exp", 0));
        data.setQinglongCoins(playerSection.getInt("qinglongCoins", 0));
        List<String> unlockedList = playerSection.getStringList("unlockedAnimals");
        Set<String> unlocked = new HashSet<>(unlockedList);
        data.setUnlockedAnimals(unlocked);
        ConfigurationSection equipSection = playerSection.getConfigurationSection("equipmentUpgrades");
        Map<String, Integer> upgrades = new HashMap<>();
        if (equipSection != null) {
            for (String key : equipSection.getKeys(false)) {
                upgrades.put(key, equipSection.getInt(key, 0));
            }
        }
        data.setEquipmentUpgrades(upgrades);
        return data;
    }

    private void savePlayerData(Player player, PlayerData data) {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        String uuid = player.getUniqueId().toString();
        ConfigurationSection playerSection = cfg.createSection(uuid);
        playerSection.set("level", data.getLevel());
        playerSection.set("exp", data.getExp());
        playerSection.set("qinglongCoins", data.getQinglongCoins());
        playerSection.set("unlockedAnimals", new ArrayList<>(data.getUnlockedAnimals()));
        ConfigurationSection equipSection = playerSection.createSection("equipmentUpgrades");
        for (Map.Entry<String, Integer> entry : data.getEquipmentUpgrades().entrySet()) {
            equipSection.set(entry.getKey(), entry.getValue());
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}