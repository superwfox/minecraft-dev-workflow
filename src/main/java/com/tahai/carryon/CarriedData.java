package com.tahai.carryon;

import java.util.List;
import java.util.UUID;

/**
 * 搬运物数据结构POJO，记录搬运类型（ENTITY或BLOCK），实体引用或方块NBT数据，方块容器内物品NBT等。
 * 不依赖任何Bukkit API。
 */
public record CarriedData(
        CarriedType type,
        UUID entityUUID,
        String blockWorld,
        int blockX,
        int blockY,
        int blockZ,
        String blockNBT,
        List<String> containerItemsNBT
) {

    /**
     * 搬运类型枚举
     */
    public enum CarriedType {
        ENTITY,
        BLOCK
    }

    // 便捷构造方法：仅用于实体搬运
    public static CarriedData forEntity(UUID entityUUID) {
        return new CarriedData(
                CarriedType.ENTITY,
                entityUUID,
                null,
                0, 0, 0,
                null,
                null
        );
    }

    // 便捷构造方法：仅用于方块搬运
    public static CarriedData forBlock(String world, int x, int y, int z, String blockNBT, List<String> itemsNBT) {
        return new CarriedData(
                CarriedType.BLOCK,
                null,
                world,
                x, y, z,
                blockNBT,
                itemsNBT
        );
    }
}