package com.tahai.baoshi;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InventoryClickListener implements Listener {

    private final DataManager dataManager;
    private final Map<UUID, GemSession> sessions = new HashMap<>();

    public InventoryClickListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClick() != ClickType.LEFT) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // 合成流程：光标上是粘合剂，点击的是宝石
        if (isGlue(cursor) && isGem(current)) {
            event.setCancelled(true);
            handleCraft(player, cursor, current);
            return;
        }

        // 镶嵌流程：光标上是宝石，点击的是装备
        if (isGem(cursor) && isEquipment(current)) {
            event.setCancelled(true);
            handleSocket(player, cursor, current, event);
            return;
        }

        // 其他左键点击，重置合成会话
        UUID playerId = player.getUniqueId();
        if (sessions.containsKey(playerId)) {
            sessions.remove(playerId);
            player.sendMessage(ChatColor.GRAY + "合成流程已取消。");
        }
    }

    private boolean isGlue(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().contains("粘合剂");
    }

    private boolean isGem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return NbtUtil.getUUID(item) != null;
    }

    private boolean isEquipment(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String name = item.getType().name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
               name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
               name.endsWith("_SWORD") || name.endsWith("_AXE") ||
               name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") ||
               name.endsWith("_HOE") || name.endsWith("_BOW");
    }

    private void handleCraft(Player player, ItemStack glue, ItemStack gem) {
        UUID gemUuid = NbtUtil.getUUID(gem);
        String gemType = dataManager.getGemType(gemUuid);
        int gemLevel = dataManager.getGemLevel(gemUuid);
        if (gemType == null || gemLevel <= 0) {
            player.sendMessage(ChatColor.AQUA + "无效的宝石。");
            return;
        }

        UUID playerId = player.getUniqueId();
        GemSession session = sessions.get(playerId);

        if (session == null) {
            session = new GemSession(gemType, gemLevel);
            session.gemIds.add(gemUuid);
            sessions.put(playerId, session);
            player.sendMessage(ChatColor.YELLOW + "合成开始，已选择第1颗宝石。");
        } else {
            if (!session.gemType.equals(gemType) || session.gemLevel != gemLevel) {
                sessions.remove(playerId);
                player.sendMessage(ChatColor.GRAY + "宝石类型或等级不一致，合成流程已重置。");
                return;
            }
            if (session.gemIds.contains(gemUuid)) {
                player.sendMessage(ChatColor.GRAY + "这颗宝石已经在合成队列中。");
                return;
            }
            session.gemIds.add(gemUuid);
            player.sendMessage(ChatColor.YELLOW + "已选择第" + session.gemIds.size() + "颗宝石。");

            if (session.gemIds.size() >= 4) {
                if (glue.getAmount() < 1) {
                    player.sendMessage(ChatColor.AQUA + "粘合剂不足。");
                    sessions.remove(playerId);
                    return;
                }
                // 移除宝石
                for (UUID uuid : session.gemIds) {
                    if (!removeItemByUUID(player, uuid)) {
                        player.sendMessage(ChatColor.AQUA + "无法找到宝石，合成失败。");
                        sessions.remove(playerId);
                        return;
                    }
                }
                // 消耗粘合剂
                if (glue.getAmount() > 1) {
                    glue.setAmount(glue.getAmount() - 1);
                    player.setItemOnCursor(glue);
                } else {
                    player.setItemOnCursor(null);
                }
                // 创建高级宝石
                ItemStack newGem = GemBuilder.createGem(gemType, gemLevel + 1);
                player.getInventory().addItem(newGem).values().forEach(left ->
                    player.getWorld().dropItem(player.getLocation(), left));
                player.sendMessage(ChatColor.YELLOW + "合成成功！获得高级宝石。");
                sessions.remove(playerId);
            }
        }
    }

    private void handleSocket(Player player, ItemStack gem, ItemStack equipment, InventoryClickEvent event) {
        UUID gemUuid = NbtUtil.getUUID(gem);
        String gemType = dataManager.getGemType(gemUuid);
        int gemLevel = dataManager.getGemLevel(gemUuid);
        if (gemType == null || gemLevel <= 0) {
            player.sendMessage(ChatColor.AQUA + "无效的宝石。");
            return;
        }

        UUID equipUuid = NbtUtil.getUUID(equipment);
        if (equipUuid != null) {
            player.sendMessage(ChatColor.AQUA + "该装备已镶嵌宝石。");
            return;
        }

        if (!canSocket(gemType, equipment.getType())) {
            player.sendMessage(ChatColor.AQUA + "该装备无法镶嵌此类宝石。");
            return;
        }

        ItemStack newEquip = equipment.clone();
        newEquip = NbtUtil.setUUID(newEquip, gemUuid);
        player.setItemOnCursor(null);
        event.getView().setItem(event.getRawSlot(), newEquip);
        dataManager.setGemData(gemUuid, gemType, gemLevel);
        player.sendMessage(ChatColor.YELLOW + "镶嵌成功！");
    }

    private boolean canSocket(String gemType, Material type) {
        String name = type.name();
        if (gemType.equalsIgnoreCase("ATTACK")) {
            return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_PICKAXE") ||
                   name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("_BOW");
        } else if (gemType.equalsIgnoreCase("DEFENSE")) {
            return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                   name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
        }
        return true;
    }

    private boolean removeItemByUUID(Player player, UUID uuid) {
        PlayerInventory inv = player.getInventory();
        // 主背包
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && !item.getType().equals(Material.AIR)) {
                UUID id = NbtUtil.getUUID(item);
                if (id != null && uuid.equals(id)) {
                    inv.setItem(i, null);
                    return true;
                }
            }
        }
        // 盔甲栏
        ItemStack[] armor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item != null && !item.getType().equals(Material.AIR)) {
                UUID id = NbtUtil.getUUID(item);
                if (id != null && uuid.equals(id)) {
                    armor[i] = null;
                    inv.setArmorContents(armor);
                    return true;
                }
            }
        }
        // 副手
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand != null && !offhand.getType().equals(Material.AIR)) {
            UUID id = NbtUtil.getUUID(offhand);
            if (id != null && uuid.equals(id)) {
                inv.setItemInOffHand(null);
                return true;
            }
        }
        // 光标
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && !cursor.getType().equals(Material.AIR)) {
            UUID id = NbtUtil.getUUID(cursor);
            if (id != null && uuid.equals(id)) {
                player.setItemOnCursor(null);
                return true;
            }
        }
        return false;
    }

    private static class GemSession {
        String gemType;
        int gemLevel;
        List<UUID> gemIds = new ArrayList<>();

        GemSession(String gemType, int gemLevel) {
            this.gemType = gemType;
            this.gemLevel = gemLevel;
        }
    }
}