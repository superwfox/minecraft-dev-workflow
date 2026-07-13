package com.tahai.randomfishingloot;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootManager {

    private final ConfigManager configManager;
    private final Random random;

    public LootManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.random = new Random();
    }

    /**
     * Generates a random fishing loot item based on configuration.
     *
     * @return generated ItemStack, or null if equipment pool is empty or all entries are invalid
     */
    public ItemStack generateRandomLoot() {
        // 1. 从配置获取装备池并随机选择
        List<String> pool = configManager.getEquipmentPool();
        if (pool == null || pool.isEmpty()) {
            return null;
        }
        String materialName = pool.get(random.nextInt(pool.size()));

        // 2. 解析 Material
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null || !material.isItem()) {
            return null;
        }

        // 3. 创建 ItemStack
        ItemStack item = new ItemStack(material, 1);

        // 4. 附魔处理
        int minEnchants = configManager.getEnchantMin();
        int maxEnchants = configManager.getEnchantMax();
        if (minEnchants < 1) minEnchants = 1;
        if (maxEnchants < minEnchants) maxEnchants = minEnchants;

        // 收集该物品可使用的所有附魔
        List<Enchantment> applicable = new ArrayList<>();
        for (Enchantment ench : Registry.ENCHANTMENT) {
            if (ench.canEnchantItem(item)) {
                applicable.add(ench);
            }
        }

        if (!applicable.isEmpty()) {
            int enchCount = random.nextInt(maxEnchants - minEnchants + 1) + minEnchants;
            if (enchCount > applicable.size()) {
                enchCount = applicable.size();
            }
            // 随机抽取 enchCount 个附魔
            for (int i = 0; i < enchCount; i++) {
                int idx = random.nextInt(applicable.size());
                Enchantment selected = applicable.remove(idx);  // 移除以免重复
                int level = random.nextInt(selected.getMaxLevel()) + 1;  // 1 ~ maxLevel
                item.addUnsafeEnchantment(selected, level);
            }
        }

        // 5. 随机耐久损伤
        short maxDura = material.getMaxDurability();
        if (maxDura > 0) {
            int damage = random.nextInt(maxDura);  // 0 ~ maxDura-1
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(damage);
                item.setItemMeta(meta);
            }
        }

        return item;
    }
}