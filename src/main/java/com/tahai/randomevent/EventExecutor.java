package com.tahai.randomevent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventExecutor {

    public enum EventType {
        SPAWN_MOBS,
        CHANGE_GAMEMODE,
        APPLY_BUFF,
        TNT_RAIN,
        REPLACE_BLOCKS,
        SHUFFLE_INVENTORY,
        BUILD_STRUCTURE,
        TELEPORT
    }

    private static final Map<UUID, Long> eventStartTimes = new ConcurrentHashMap<>();
    private static final Map<UUID, EventType> activeEventTypes = new ConcurrentHashMap<>();

    // 基础权重，可随通过次数调整
    private static final Map<EventType, Integer> baseWeights = new LinkedHashMap<>();
    static {
        baseWeights.put(EventType.SPAWN_MOBS, 50);
        baseWeights.put(EventType.CHANGE_GAMEMODE, 40);
        baseWeights.put(EventType.APPLY_BUFF, 60);
        baseWeights.put(EventType.TNT_RAIN, 30);
        baseWeights.put(EventType.REPLACE_BLOCKS, 50);
        baseWeights.put(EventType.SHUFFLE_INVENTORY, 40);
        baseWeights.put(EventType.BUILD_STRUCTURE, 20);
        baseWeights.put(EventType.TELEPORT, 60);
    }

    /**
     * 根据玩家通过次数选择并执行一个随机事件。
     * @param player 目标玩家
     * @return 被触发的事件类型
     */
    public static EventType executeRandomEvent(Player player) {
        UUID uuid = player.getUniqueId();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("RandomEvent");
        PlayerDataManager pdm = new PlayerDataManager(plugin);
        int passCount = pdm.getPassCount(uuid);

        // 根据通过次数调整权重：通过越多，战斗/负面事件权重增加
        Map<EventType, Integer> adjustedWeights = new LinkedHashMap<>(baseWeights);
        adjustedWeights.computeIfPresent(EventType.SPAWN_MOBS, (k, v) -> v + passCount * 5);
        adjustedWeights.computeIfPresent(EventType.TNT_RAIN, (k, v) -> v + passCount * 3);
        adjustedWeights.computeIfPresent(EventType.REPLACE_BLOCKS, (k, v) -> v + passCount * 2);
        adjustedWeights.computeIfPresent(EventType.APPLY_BUFF, (k, v) -> v - passCount * 2);
        // 保证最小权重为 1
        adjustedWeights.replaceAll((k, v) -> Math.max(v, 1));

        // 加权随机选择
        EventType chosen = weightedRandom(adjustedWeights);

        // 记录事件开始时间
        eventStartTimes.put(uuid, System.currentTimeMillis());
        activeEventTypes.put(uuid, chosen);

        // 执行效果
        switch (chosen) {
            case SPAWN_MOBS -> spawnMobs(player);
            case CHANGE_GAMEMODE -> changeGameMode(player);
            case APPLY_BUFF -> applyBuff(player);
            case TNT_RAIN -> tntRain(player);
            case REPLACE_BLOCKS -> replaceBlocks(player);
            case SHUFFLE_INVENTORY -> shuffleInventory(player);
            case BUILD_STRUCTURE -> buildStructure(player);
            case TELEPORT -> teleportPlayer(player);
        }

        return chosen;
    }

    // ========== 效果实现 ==========

    private static void spawnMobs(Player player) {
        World world = player.getWorld();
        Location center = player.getLocation();
        Random rand = new Random();
        int count = 5 + rand.nextInt(6); // 5~10
        List<EntityType> mobs = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER);
        for (int i = 0; i < count; i++) {
            double dx = (rand.nextDouble() - 0.5) * 20;
            double dz = (rand.nextDouble() - 0.5) * 20;
            Location spawnLoc = center.clone().add(dx, 0, dz);
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc));
            EntityType type = mobs.get(rand.nextInt(mobs.size()));
            world.spawnEntity(spawnLoc, type);
        }
    }

    private static void changeGameMode(Player player) {
        // 切换为冒险模式（如果已经是则改为生存）
        if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
            player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        } else if (player.getGameMode() == org.bukkit.GameMode.ADVENTURE) {
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        } else {
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        }
    }

    private static void applyBuff(Player player) {
        Random rand = new Random();
        PotionEffectType[] effects = {
            PotionEffectType.SPEED,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.REGENERATION,
            PotionEffectType.DAMAGE_RESISTANCE,
            PotionEffectType.WEAKNESS,
            PotionEffectType.SLOWNESS,
            PotionEffectType.POISON
        };
        PotionEffectType chosen = effects[rand.nextInt(effects.length)];
        int duration = 1200; // 60 秒 * 20 ticks
        int amplifier = rand.nextInt(2); // 0 或 1
        player.addPotionEffect(new PotionEffect(chosen, duration, amplifier, false, true));
    }

    private static void tntRain(Player player) {
        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 20, 0);
        Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            double dx = (rand.nextDouble() - 0.5) * 10;
            double dz = (rand.nextDouble() - 0.5) * 10;
            Location tntLoc = loc.clone().add(dx, 0, dz);
            TNTPrimed tnt = (TNTPrimed) world.spawnEntity(tntLoc, EntityType.TNT);
            tnt.setFuseTicks(30 + rand.nextInt(40)); // 1.5~3.5 秒爆炸
        }
    }

    private static void replaceBlocks(Player player) {
        World world = player.getWorld();
        Location center = player.getLocation();
        Random rand = new Random();
        Material[] replacements = {Material.SAND, Material.GRAVEL, Material.COBBLESTONE, Material.DIRT};
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    if (rand.nextDouble() < 0.3) {
                        Block block = center.clone().add(x, y, z).getBlock();
                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                            block.setType(replacements[rand.nextInt(replacements.length)]);
                        }
                    }
                }
            }
        }
    }

    private static void shuffleInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        List<ItemStack> items = new ArrayList<>(Arrays.asList(contents));
        Collections.shuffle(items, new Random());
        player.getInventory().setContents(items.toArray(new ItemStack[0]));
    }

    private static void buildStructure(Player player) {
        World world = player.getWorld();
        Location start = player.getLocation().getBlock().getLocation();
        // 小石屋：5x5 底座，3 格高，部分空心
        Random rand = new Random();
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                for (int y = 0; y < 3; y++) {
                    Location loc = start.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    if (y == 0) {
                        block.setType(Material.STONE);
                    } else if (x == 0 || x == 4 || z == 0 || z == 4) {
                        block.setType(Material.STONE_BRICKS);
                    } else if (y == 2) {
                        block.setType(Material.STONE_BRICK_SLAB);
                    } else {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
        // 放置一个门
        Location doorLoc = start.clone().add(2, 1, 0);
        doorLoc.getBlock().setType(Material.OAK_DOOR);
    }

    private static void teleportPlayer(Player player) {
        World world = player.getWorld();
        Random rand = new Random();
        double x = rand.nextDouble() * 2000 - 1000;
        double z = rand.nextDouble() * 2000 - 1000;
        int y = world.getHighestBlockYAt((int) x, (int) z);
        if (y < 0) y = 64;
        Location target = new Location(world, x, y + 1, z);
        player.teleport(target);
    }

    // ========== 辅助方法 ==========

    private static EventType weightedRandom(Map<EventType, Integer> weights) {
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        int random = new Random().nextInt(total);
        int cumulative = 0;
        for (Map.Entry<EventType, Integer> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (random < cumulative) {
                return entry.getKey();
            }
        }
        return EventType.SPAWN_MOBS; // fallback
    }

    // 公开方法，用于获取事件开始时间（供清除逻辑使用）
    public static long getEventStartTime(UUID playerUuid) {
        return eventStartTimes.getOrDefault(playerUuid, 0L);
    }

    public static EventType getActiveEventType(UUID playerUuid) {
        return activeEventTypes.get(playerUuid);
    }

    public static void clearEventData(UUID playerUuid) {
        eventStartTimes.remove(playerUuid);
        activeEventTypes.remove(playerUuid);
    }
}