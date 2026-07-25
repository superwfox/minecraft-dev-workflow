package com.tahai.baoshi;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public final class GemBuilder {

    private GemBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack createGem(String type, int level) {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "宝石 · " + type + " Lv." + level);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "类型: " + type,
                ChatColor.GRAY + "等级: " + level
        ));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createGlue() {
        ItemStack item = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "宝石粘合剂");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "用于将宝石镶嵌到装备上"));
        item.setItemMeta(meta);
        return item;
    }
}