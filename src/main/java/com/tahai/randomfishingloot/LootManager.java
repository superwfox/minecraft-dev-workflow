package com.tahai.randomfishingloot;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class LootManager {

    private LootManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack generateRandomLoot(List<String> equipmentPool, int minEnchants, int maxEnchants, Random random) {
        if (equipmentPool == null || equipmentPool.isEmpty()) {
            return null;
        }

        Material material = null;
        int attempts = 0;
        while (material == null && attempts < 10) {
            String matName = equipmentPool.get(random.nextInt(equipmentPool.size()));
            material = Material.matchMaterial(matName, false);
            attempts++;
        }
        if (material == null) {
            return null;
        }

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        // 获取适用附魔列表
        List<Enchantment> possible = new ArrayList<>();
        for (Enchantment ench : Enchantment.values()) {
            if (ench.canEnchantItem(item)) {
                possible.add(ench);
            }
        }
        if (possible.isEmpty()) {
            // 无可用附魔但装备本身有效
            return item;
        }

        // 随机附魔数量（不超过可用列表大小）
        int count = Math.min(random.nextInt(maxEnchants - minEnchants + 1) + minEnchants, possible.size());
        List<Enchantment> copy = new ArrayList<>(possible);
        for (int i = 0; i < count; i++) {
            Enchantment ench = copy.remove(random.nextInt(copy.size()));
            int level = random.nextInt(ench.getMaxLevel()) + 1;
            meta.addEnchant(ench, level, true);
        }

        // 随机损伤（仅可损坏物品）
        if (meta instanceof Damageable damageable) {
            short maxDura = material.getMaxDurability();
            if (maxDura > 0) {
                damageable.setDamage((short) (random.nextInt(maxDura)));
            }
        }

        item.setItemMeta(meta);
        return item;
    }
}