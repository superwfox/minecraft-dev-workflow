package com.tahai.baoshi;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class GemHelper {

    private GemHelper() {} // 防止实例化

    // 存储自定义数据的 Lore 行前缀（使用灰色，符合配色规范）
    private static final String DATA_PREFIX = ChatColor.GRAY.toString();

    public enum GemType {
        RUBY("红宝石"),
        SAPPHIRE("蓝宝石"),
        EMERALD("绿宝石"),
        DIAMOND("钻石");

        private final String displayName;

        GemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 创建宝石物品
     * @param type  宝石类型
     * @param level 等级 (1~5)
     * @return 宝石物品
     */
    public static ItemStack createGemItem(GemType type, int level) {
        ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "✦ " + type.getDisplayName() + ChatColor.GRAY + " Lv." + level);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "一颗充满魔力的宝石");
        lore.add(ChatColor.GRAY + "等级: " + ChatColor.WHITE + level);
        lore.add(ChatColor.GRAY + "属性加成:");
        Map<String, Double> bonus = getAttributeBonus(type, level);
        for (Map.Entry<String, Double> entry : bonus.entrySet()) {
            lore.add(ChatColor.GRAY + "  " + entry.getKey() + ": " + ChatColor.YELLOW + "+" + entry.getValue());
        }
        lore.add(""); // 空行
        lore.add(DATA_PREFIX + type.name() + ";" + level);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 创建粘合剂物品
     */
    public static ItemStack createGlueItem() {
        ItemStack item = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "✿ 粘合剂");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "用于将宝石镶嵌到装备上");
        lore.add(ChatColor.GRAY + "右键装备即可使用");
        lore.add(DATA_PREFIX + "glue");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 从物品 Lore 中读取宝石类型
     */
    public static GemType getGemType(ItemStack item) {
        String data = extractData(item);
        if (data == null || data.isEmpty() || data.equals("glue")) return null;
        String[] parts = data.split(";");
        if (parts.length != 2) return null;
        try {
            return GemType.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 从物品 Lore 中读取宝石等级
     */
    public static int getGemLevel(ItemStack item) {
        String data = extractData(item);
        if (data == null || data.isEmpty() || data.equals("glue")) return -1;
        String[] parts = data.split(";");
        if (parts.length != 2) return -1;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 将宝石数据写入物品 Lore
     */
    public static void setGemData(ItemStack item, GemType type, int level) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        // 移除旧数据行
        lore.removeIf(line -> line.startsWith(DATA_PREFIX));
        lore.add(DATA_PREFIX + type.name() + ";" + level);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * 判断物品是否为宝石
     */
    public static boolean isGem(ItemStack item) {
        GemType type = getGemType(item);
        return type != null;
    }

    /**
     * 判断物品是否为粘合剂
     */
    public static boolean isGlue(ItemStack item) {
        String data = extractData(item);
        return "glue".equals(data);
    }

    /**
     * 获取宝石的属性加成（键-属性名，值-加成数值）
     */
    public static Map<String, Double> getAttributeBonus(GemType type, int level) {
        Map<String, Double> map = new LinkedHashMap<>();
        double multiplier = level * 0.5; // 每级增加50% 基础值
        switch (type) {
            case RUBY:
                map.put("攻击力", 2.0 * multiplier);
                break;
            case SAPPHIRE:
                map.put("防御力", 1.5 * multiplier);
                break;
            case EMERALD:
                map.put("生命值", 4.0 * multiplier);
                break;
            case DIAMOND:
                map.put("暴击率", 1.0 * multiplier);
                map.put("暴击伤害", 1.5 * multiplier);
                break;
        }
        return map;
    }

    /**
     * 检查物品类型是否允许镶嵌指定宝石
     */
    public static boolean canApplyToItem(GemType type, ItemStack target) {
        if (target == null || target.getType() == Material.AIR) return false;
        Material mat = target.getType();
        // 宝石只能镶嵌在武器、工具、盔甲上
        return mat.name().contains("SWORD") ||
               mat.name().contains("AXE") ||
               mat.name().contains("PICKAXE") ||
               mat.name().contains("SHOVEL") ||
               mat.name().contains("HOE") ||
               mat.name().contains("HELMET") ||
               mat.name().contains("CHESTPLATE") ||
               mat.name().contains("LEGGINGS") ||
               mat.name().contains("BOOTS") ||
               mat == Material.BOW ||
               mat == Material.FISHING_ROD;
    }

    // 从物品 Lore 中提取数据行（不含前缀）
    private static String extractData(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return null;
        for (String line : lore) {
            if (line.startsWith(DATA_PREFIX)) {
                // 移除前缀后即为纯数据
                return line.substring(DATA_PREFIX.length());
            }
        }
        return null;
    }
}