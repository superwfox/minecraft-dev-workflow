package com.tahai.qinglong.gui;

import com.tahai.qinglong.manager.ConfigManager;
import com.tahai.qinglong.manager.DataManager;
import com.tahai.qinglong.model.AnimalData;
import com.tahai.qinglong.model.MapData;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenuGUI implements InventoryHolder, Listener {

    private Inventory inventory;
    private String menuType;      // "main", "map", "animal"
    private String selectedMapKey;
    private String selectedAnimalKey;

    private static ConfigManager configManager;
    private static DataManager dataManager;

    private static void ensureManagers() {
        if (configManager != null) return;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Qinglong");
        if (plugin == null) {
            Bukkit.getLogger().severe("[Qinglong] Plugin not found, cannot initialize GUI managers.");
            return;
        }
        configManager = new ConfigManager((JavaPlugin) plugin);
        dataManager = new DataManager(plugin);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void setInventory(Inventory inv) {
        this.inventory = inv;
    }

    public String getMenuType() {
        return menuType;
    }

    public String getSelectedMapKey() {
        return selectedMapKey;
    }

    public String getSelectedAnimalKey() {
        return selectedAnimalKey;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Inventory click handling can be implemented later
    }

    // ---------- 静态打开方法 ----------

    public static void openMainMenu(Player player) {
        ensureManagers();
        MenuGUI holder = new MenuGUI();
        holder.menuType = "main";

        Inventory inv = Bukkit.createInventory(holder, 27, ChatColor.GRAY + "青龙菜单");
        holder.setInventory(inv);

        // 加载玩家数据
        var data = dataManager.loadPlayer(player);
        int level = data.getLevel();
        int coins = data.getQinglongCoins();

        // 地图传送物品
        ItemStack mapItem = createItem(Material.MAP, ChatColor.YELLOW + "地图传送",
                ChatColor.GRAY + "选择要前往的地图",
                ChatColor.GRAY + "当前等级: " + level);
        inv.setItem(11, mapItem);

        // 宠物选择物品
        ItemStack animalItem = createItem(Material.COW_SPAWN_EGG, ChatColor.YELLOW + "宠物选择",
                ChatColor.GRAY + "选择已经解锁的宠物",
                ChatColor.GRAY + "青龙币: " + coins);
        inv.setItem(15, animalItem);

        // 装饰边框
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, border);
            }
        }

        player.openInventory(inv);
    }

    public static void openMapSelection(Player player) {
        ensureManagers();
        MenuGUI holder = new MenuGUI();
        holder.menuType = "map";

        // 从配置读取所有地图 key
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Qinglong");
        if (plugin == null) return;
        JavaPlugin javaPlugin = (JavaPlugin) plugin;
        Set<String> keys = javaPlugin.getConfig().getConfigurationSection("maps").getKeys(false);
        int size = ((keys.size() / 9) + 1) * 9;
        if (size < 27) size = 27;

        Inventory inv = Bukkit.createInventory(holder, size, ChatColor.GRAY + "选择地图");
        holder.setInventory(inv);

        int slot = 0;
        for (String key : keys) {
            MapData mapData = configManager.getMapData(key);
            if (mapData == null) continue;

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + mapData.getDisplayName());
            lore.add(ChatColor.GRAY + "世界: " + mapData.getWorld());
            lore.add(ChatColor.YELLOW + "点击传送");

            ItemStack item = createItem(Material.FILLED_MAP, ChatColor.YELLOW + mapData.getDisplayName(), lore.toArray(new String[0]));
            inv.setItem(slot++, item);
        }

        // 填充空位
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, border);
            }
        }

        holder.selectedMapKey = null; // 暂存，点击时由监听器设置
        player.openInventory(inv);
    }

    public static void openAnimalSelection(Player player) {
        ensureManagers();
        MenuGUI holder = new MenuGUI();
        holder.menuType = "animal";

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Qinglong");
        if (plugin == null) return;
        JavaPlugin javaPlugin = (JavaPlugin) plugin;
        Set<String> keys = javaPlugin.getConfig().getConfigurationSection("animals").getKeys(false);
        int size = ((keys.size() / 9) + 1) * 9;
        if (size < 27) size = 27;

        Inventory inv = Bukkit.createInventory(holder, size, ChatColor.GRAY + "选择宠物");
        holder.setInventory(inv);

        var data = dataManager.loadPlayer(player);
        Set<String> unlocked = data.getUnlockedAnimals();

        int slot = 0;
        for (String key : keys) {
            AnimalData animalData = configManager.getAnimalData(key);
            if (animalData == null) continue;

            boolean isUnlocked = unlocked.contains(key);
            Material material = isUnlocked ? Material.COW_SPAWN_EGG : Material.BARRIER;
            String display = isUnlocked
                    ? ChatColor.YELLOW + animalData.getDisplayName()
                    : ChatColor.AQUA + animalData.getDisplayName() + " (未解锁)";
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + animalData.getDisplayName());
            lore.add(ChatColor.GRAY + "解锁等级: " + animalData.getUnlockLevel());
            if (isUnlocked) {
                lore.add(ChatColor.YELLOW + "点击召唤");
            } else {
                lore.add(ChatColor.AQUA + "需要等级 " + animalData.getUnlockLevel());
            }

            ItemStack item = createItem(material, display, lore.toArray(new String[0]));
            inv.setItem(slot++, item);
        }

        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, border);
        }

        holder.selectedAnimalKey = null;
        player.openInventory(inv);
    }

    // ---------- 辅助方法 ----------

    private static ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (loreLines != null && loreLines.length > 0) {
                List<String> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(line);
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}