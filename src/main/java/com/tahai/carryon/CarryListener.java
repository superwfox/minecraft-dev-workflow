package com.tahai.carryon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CarryListener implements Listener {
    private static Plugin plugin;
    private static CarryManager manager;
    private static NamespacedKey KEY_TYPE;
    private static NamespacedKey KEY_ENTITY_UUID;
    private static NamespacedKey KEY_ENTITY_TYPE;
    private static NamespacedKey KEY_BLOCK_MATERIAL;
    private static NamespacedKey KEY_BLOCK_WORLD;
    private static NamespacedKey KEY_BLOCK_X;
    private static NamespacedKey KEY_BLOCK_Y;
    private static NamespacedKey KEY_BLOCK_Z;
    private static NamespacedKey KEY_BLOCK_ITEMS;

    // 头顶显示实体的映射
    private static final Map<Player, Entity> displayEntities = new HashMap<>();

    public static void init(Plugin pl, CarryManager mgr) {
        plugin = pl;
        manager = mgr;
        KEY_TYPE = new NamespacedKey(pl, "carry_type");
        KEY_ENTITY_UUID = new NamespacedKey(pl, "entity_uuid");
        KEY_ENTITY_TYPE = new NamespacedKey(pl, "entity_type");
        KEY_BLOCK_MATERIAL = new NamespacedKey(pl, "block_material");
        KEY_BLOCK_WORLD = new NamespacedKey(pl, "block_world");
        KEY_BLOCK_X = new NamespacedKey(pl, "block_x");
        KEY_BLOCK_Y = new NamespacedKey(pl, "block_y");
        KEY_BLOCK_Z = new NamespacedKey(pl, "block_z");
        KEY_BLOCK_ITEMS = new NamespacedKey(pl, "block_items");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() != Material.AIR) {
            return;
        }
        CarriedData carried = manager.getCarried(player);
        if (carried != null) {
            handlePlace(event, player, carried);
        } else if (player.isSneaking()) {
            handlePickup(event, player);
        }
    }

    private void handlePickup(PlayerInteractEvent event, Player player) {
        // 移除旧的显示实体
        removeDisplayEntity(player);

        Entity targetEntity = event.getRightClicked();
        if (targetEntity != null) {
            // 直接使用被右键的实体，不再循环getVehicle()
            if (targetEntity instanceof Player) {
                player.sendMessage(ChatColor.AQUA + "不能搬运玩家");
                event.setCancelled(true);
                return;
            }
            if (!manager.isEntityAllowed(targetEntity.getType())) {
                player.sendMessage(ChatColor.AQUA + "你不能搬运这个实体");
                event.setCancelled(true);
                return;
            }
            // 检查显示实体自身（防止玩家点击自己的显示实体）
            if (displayEntities.get(player) != null && displayEntities.get(player).equals(targetEntity)) {
                event.setCancelled(true);
                return;
            }
            // 存储 PDC 信息
            PersistentDataContainer pdc = player.getPersistentDataContainer();
            pdc.set(KEY_TYPE, PersistentDataType.STRING, "ENTITY");
            pdc.set(KEY_ENTITY_UUID, PersistentDataType.STRING, targetEntity.getUniqueId().toString());
            pdc.set(KEY_ENTITY_TYPE, PersistentDataType.STRING, targetEntity.getType().getKey().toString());
            // 移除原实体
            targetEntity.remove();
            CarriedData data = CarriedData.forEntity(targetEntity.getUniqueId());
            manager.setCarried(player, data);
            player.sendMessage(ChatColor.YELLOW + "已拾取 " + targetEntity.getType().name());
            // 创建头顶显示
            createDisplayEntity(player, data);
            event.setCancelled(true);
            return;
        }
        if (event.getClickedBlock() != null) {
            org.bukkit.block.Block block = event.getClickedBlock();
            if (!manager.isBlockAllowed(block.getType())) {
                player.sendMessage(ChatColor.AQUA + "你不能搬运这个方块");
                event.setCancelled(true);
                return;
            }
            // 序列化容器物品（如果有）
            List<String> itemsNBT = new ArrayList<>();
            if (block.getState() instanceof InventoryHolder) {
                Inventory inv = ((InventoryHolder) block.getState()).getInventory();
                String base64 = itemStackArrayToBase64(inv.getContents());
                if (!base64.isEmpty()) {
                    itemsNBT.add(base64);
                }
            }
            // 存储 PDC
            PersistentDataContainer pdc = player.getPersistentDataContainer();
            pdc.set(KEY_TYPE, PersistentDataType.STRING, "BLOCK");
            pdc.set(KEY_BLOCK_MATERIAL, PersistentDataType.STRING, block.getType().name());
            pdc.set(KEY_BLOCK_WORLD, PersistentDataType.STRING, block.getWorld().getName());
            pdc.set(KEY_BLOCK_X, PersistentDataType.INTEGER, block.getX());
            pdc.set(KEY_BLOCK_Y, PersistentDataType.INTEGER, block.getY());
            pdc.set(KEY_BLOCK_Z, PersistentDataType.INTEGER, block.getZ());
            pdc.set(KEY_BLOCK_ITEMS, PersistentDataType.STRING, ""); // 保留但不使用
            // 移除方块
            block.setType(Material.AIR);
            CarriedData data = CarriedData.forBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), "{}", itemsNBT);
            manager.setCarried(player, data);
            player.sendMessage(ChatColor.YELLOW + "已拾取 " + block.getType().name());
            // 创建头顶显示
            createDisplayEntity(player, data);
            event.setCancelled(true);
        }
    }

    private void handlePlace(PlayerInteractEvent event, Player player, CarriedData carried) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        String type = pdc.get(KEY_TYPE, PersistentDataType.STRING);
        if (type == null) {
            manager.removeCarried(player);
            removeDisplayEntity(player);
            return;
        }
        Location placeLoc = null;
        org.bukkit.block.Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            placeLoc = clickedBlock.getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);
        } else {
            placeLoc = player.getLocation();
        }
        World world = placeLoc.getWorld();
        if (world == null) return;

        boolean success = false;
        if ("ENTITY".equals(type)) {
            String entityTypeStr = pdc.get(KEY_ENTITY_TYPE, PersistentDataType.STRING);
            if (entityTypeStr == null) {
                manager.removeCarried(player);
                removeDisplayEntity(player);
                return;
            }
            EntityType entityType = EntityType.fromKey(org.bukkit.NamespacedKey.fromString(entityTypeStr));
            if (entityType == null) {
                player.sendMessage(ChatColor.AQUA + "无法放置，实体类型无效");
                manager.removeCarried(player);
                removeDisplayEntity(player);
                return;
            }
            List<Entity> nearby = world.getNearbyEntities(placeLoc, 0.5, 0.5, 0.5);
            if (!nearby.isEmpty()) {
                player.sendMessage(ChatColor.AQUA + "这个位置没有空间放置实体");
                event.setCancelled(true);
                return;
            }
            world.spawnEntity(placeLoc, entityType);
            success = true;
            pdc.remove(KEY_TYPE);
            pdc.remove(KEY_ENTITY_UUID);
            pdc.remove(KEY_ENTITY_TYPE);
        } else if ("BLOCK".equals(type)) {
            String materialStr = pdc.get(KEY_BLOCK_MATERIAL, PersistentDataType.STRING);
            if (materialStr == null) {
                manager.removeCarried(player);
                removeDisplayEntity(player);
                return;
            }
            Material material = Material.getMaterial(materialStr);
            if (material == null) {
                player.sendMessage(ChatColor.AQUA + "无法放置，方块类型无效");
                manager.removeCarried(player);
                removeDisplayEntity(player);
                return;
            }
            org.bukkit.block.Block targetBlock = placeLoc.getBlock();
            if (!targetBlock.isReplaceable()) {
                player.sendMessage(ChatColor.AQUA + "这个位置无法放置方块");
                event.setCancelled(true);
                return;
            }
            targetBlock.setType(material);
            // 尝试恢复容器内容（如果有）
            List<String> itemsNBT = carried.containerItemsNBT();
            if (!itemsNBT.isEmpty()) {
                String base64 = itemsNBT.get(0);
                if (!base64.isEmpty()) {
                    ItemStack[] contents = itemStackArrayFromBase64(base64);
                    if (targetBlock.getState() instanceof InventoryHolder) {
                        Inventory inv = ((InventoryHolder) targetBlock.getState()).getInventory();
                        inv.clear();
                        for (int i = 0; i < Math.min(contents.length, inv.getSize()); i++) {
                            if (contents[i] != null) {
                                inv.setItem(i, contents[i]);
                            }
                        }
                    }
                }
            }
            success = true;
            pdc.remove(KEY_TYPE);
            pdc.remove(KEY_BLOCK_MATERIAL);
            pdc.remove(KEY_BLOCK_WORLD);
            pdc.remove(KEY_BLOCK_X);
            pdc.remove(KEY_BLOCK_Y);
            pdc.remove(KEY_BLOCK_Z);
            pdc.remove(KEY_BLOCK_ITEMS);
        }
        if (success) {
            manager.removeCarried(player);
            removeDisplayEntity(player);
            player.sendMessage(ChatColor.YELLOW + "已放置");
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // 移除头顶显示
        removeDisplayEntity(player);
        CarriedData carried = manager.getCarried(player);
        if (carried != null) {
            dropCarried(player, carried);
            manager.removeCarried(player);
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        Entity dismounted = event.getDismounted();
        if (!(dismounted instanceof Player)) return;
        Player player = (Player) dismounted;
        CarriedData carried = manager.getCarried(player);
        if (carried == null) return;
        Entity passenger = event.getEntity();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        String type = pdc.get(KEY_TYPE, PersistentDataType.STRING);
        if ("ENTITY".equals(type)) {
            String uuidStr = pdc.get(KEY_ENTITY_UUID, PersistentDataType.STRING);
            if (uuidStr != null && UUID.fromString(uuidStr).equals(passenger.getUniqueId())) {
                manager.removeCarried(player);
                removeDisplayEntity(player);
                pdc.remove(KEY_TYPE);
                pdc.remove(KEY_ENTITY_UUID);
                pdc.remove(KEY_ENTITY_TYPE);
                player.sendMessage(ChatColor.GRAY + "搬运物被动脱离");
            }
        }
    }

    private void dropCarried(Player player, CarriedData carried) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        String type = pdc.get(KEY_TYPE, PersistentDataType.STRING);
        if (type == null) return;
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;
        if ("ENTITY".equals(type)) {
            String entityTypeStr = pdc.get(KEY_ENTITY_TYPE, PersistentDataType.STRING);
            if (entityTypeStr != null) {
                EntityType entityType = EntityType.fromKey(org.bukkit.NamespacedKey.fromString(entityTypeStr));
                if (entityType != null) {
                    world.spawnEntity(loc, entityType);
                }
            }
        } else if ("BLOCK".equals(type)) {
            String materialStr = pdc.get(KEY_BLOCK_MATERIAL, PersistentDataType.STRING);
            if (materialStr != null) {
                Material material = Material.getMaterial(materialStr);
                if (material != null && material.isBlock()) {
                    org.bukkit.block.Block block = loc.getBlock();
                    if (block.isReplaceable()) {
                        block.setType(material);
                        // 尝试恢复容器内容
                        List<String> itemsNBT = carried.containerItemsNBT();
                        if (!itemsNBT.isEmpty()) {
                            String base64 = itemsNBT.get(0);
                            if (!base64.isEmpty()) {
                                ItemStack[] contents = itemStackArrayFromBase64(base64);
                                if (block.getState() instanceof InventoryHolder) {
                                    Inventory inv = ((InventoryHolder) block.getState()).getInventory();
                                    inv.clear();
                                    for (int i = 0; i < Math.min(contents.length, inv.getSize()); i++) {
                                        if (contents[i] != null) {
                                            inv.setItem(i, contents[i]);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        world.dropItemNaturally(loc, new ItemStack(material));
                    }
                }
            }
        }
        pdc.remove(KEY_TYPE);
        pdc.remove(KEY_ENTITY_UUID);
        pdc.remove(KEY_ENTITY_TYPE);
        pdc.remove(KEY_BLOCK_MATERIAL);
        pdc.remove(KEY_BLOCK_WORLD);
        pdc.remove(KEY_BLOCK_X);
        pdc.remove(KEY_BLOCK_Y);
        pdc.remove(KEY_BLOCK_Z);
        pdc.remove(KEY_BLOCK_ITEMS);
        removeDisplayEntity(player);
    }

    // ----- 头顶显示管理 -----

    private void createDisplayEntity(Player player, CarriedData carried) {
        // 先移除已有的显示实体
        removeDisplayEntity(player);

        ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        stand.setSmall(true);
        stand.setVisible(false);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(false);
        stand.setGravity(false);

        if (carried.type() == CarriedData.CarriedType.ENTITY) {
            // 尝试使用刷怪蛋作为头盔显示
            EntityType entityType = null;
            try {
                String typeStr = player.getPersistentDataContainer().get(KEY_ENTITY_TYPE, PersistentDataType.STRING);
                if (typeStr != null) {
                    entityType = EntityType.fromKey(org.bukkit.NamespacedKey.fromString(typeStr));
                }
            } catch (Exception ignored) {}
            ItemStack helm = null;
            if (entityType != null) {
                try {
                    Material spawnEgg = Material.valueOf(entityType.name() + "_SPAWN_EGG");
                    helm = new ItemStack(spawnEgg);
                } catch (IllegalArgumentException e) {
                    // 如果没有对应的刷怪蛋，使用一个通用物品
                    helm = new ItemStack(Material.ZOMBIE_SPAWN_EGG); // 或用其它
                }
            } else {
                helm = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
            }
            stand.getEquipment().setHelmet(helm);
        } else {
            // 方块搬运：使用方块物品作为头盔
            try {
                String matStr = player.getPersistentDataContainer().get(KEY_BLOCK_MATERIAL, PersistentDataType.STRING);
                if (matStr != null) {
                    Material mat = Material.getMaterial(matStr);
                    if (mat != null) {
                        stand.getEquipment().setHelmet(new ItemStack(mat));
                    }
                }
            } catch (Exception e) {
                stand.remove();
                return;
            }
        }

        player.addPassenger(stand);
        displayEntities.put(player, stand);
    }

    private void removeDisplayEntity(Player player) {
        Entity entity = displayEntities.remove(player);
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
    }

    // ----- 自定义序列化辅助方法 -----

    private static String itemStackArrayToBase64(ItemStack[] items) {
        try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytesOut)) {
            out.writeObject(items);
            return Base64.getEncoder().encodeToString(bytesOut.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("无法序列化物品堆数组为Base64: " + e.getMessage());
            return "";
        }
    }

    private static ItemStack[] itemStackArrayFromBase64(String base64) {
        try (ByteArrayInputStream bytesIn = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream in = new BukkitObjectInputStream(bytesIn)) {
            return (ItemStack[]) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("无法从Base64反序列化物品堆数组: " + e.getMessage());
            return new ItemStack[0];
        }
    }
}