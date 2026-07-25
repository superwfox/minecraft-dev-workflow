package com.tahai.baoshi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        Inventory clickedInventory = event.getClickedInventory();

        // 合成：右键点击宝石，主手/副手持粘合剂
        if (event.isRightClick() && current != null && GemHelper.isGem(current)) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (!GemHelper.isGlue(mainHand) && !GemHelper.isGlue(offHand)) return;

            GemType type = GemHelper.getGemType(current);
            int level = GemHelper.getGemLevel(current);

            // 计算同种同等级宝石总数量
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && GemHelper.isGem(item) && GemHelper.getGemType(item) == type && GemHelper.getGemLevel(item) == level) {
                    count += item.getAmount();
                }
            }
            if (count < 4) {
                player.sendMessage(ChatColor.AQUA + "材料不足，需要4颗同种同等级宝石和1个粘合剂。");
                event.setCancelled(true);
                return;
            }

            // 消耗宝石
            int toRemove = 4;
            int curAmount = current.getAmount();
            if (curAmount >= toRemove) {
                current.setAmount(curAmount - toRemove);
                event.setCurrentItem(current);
                toRemove = 0;
            } else {
                event.setCurrentItem(null);
                toRemove -= curAmount;
            }
            if (toRemove > 0) {
                ItemStack[] contents = player.getInventory().getContents();
                for (int i = 0; i < contents.length && toRemove > 0; i++) {
                    ItemStack item = contents[i];
                    if (item != null && GemHelper.isGem(item) && GemHelper.getGemType(item) == type && GemHelper.getGemLevel(item) == level) {
                        int amount = item.getAmount();
                        if (amount <= toRemove) {
                            player.getInventory().setItem(i, null);
                            toRemove -= amount;
                        } else {
                            item.setAmount(amount - toRemove);
                            player.getInventory().setItem(i, item);
                            toRemove = 0;
                        }
                    }
                }
            }

            // 消耗粘合剂
            if (GemHelper.isGlue(mainHand)) {
                if (mainHand.getAmount() > 1) mainHand.setAmount(mainHand.getAmount() - 1);
                else player.getInventory().setItemInMainHand(null);
            } else {
                if (offHand.getAmount() > 1) offHand.setAmount(offHand.getAmount() - 1);
                else player.getInventory().setItemInOffHand(null);
            }

            // 生成高一级宝石
            ItemStack newGem = GemHelper.createGemItem(type, level + 1);
            player.getInventory().addItem(newGem).values().forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
            player.sendMessage(ChatColor.YELLOW + "合成成功！获得 " + type.name() + " Lv." + (level + 1) + " 宝石。");
            event.setCancelled(true);
            return;
        }

        // 镶嵌：左键点击，光标持有宝石，点击物品为装备
        if (event.isLeftClick() && cursor != null && GemHelper.isGem(cursor) && current != null && current.getType() != Material.AIR) {
            GemType gemType = GemHelper.getGemType(cursor);
            int gemLevel = GemHelper.getGemLevel(cursor);

            if (!GemHelper.canApplyToItem(gemType, current)) {
                player.sendMessage(ChatColor.AQUA + "该宝石无法镶嵌在此装备上。");
                event.setCancelled(true);
                return;
            }

            GemType existingType = GemHelper.getGemType(current);
            if (existingType != null && existingType == gemType) {
                player.sendMessage(ChatColor.AQUA + "此装备已镶嵌同种宝石。");
                event.setCancelled(true);
                return;
            }

            // 消耗宝石
            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
                event.setCursor(cursor);
            } else {
                event.setCursor(null);
            }

            // 写入镶嵌数据
            GemHelper.setGemData(current, gemType, gemLevel);
            event.setCurrentItem(current);
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "镶嵌成功！");
        }
    }
}