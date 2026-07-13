package com.tahai.containersidepanel.mapping;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.util.*;

/**
 * 维护背包内所有可展示容器（潜影盒/末影箱/收纳袋）的物品合并映射。
 * NBT完全匹配，无堆叠限制。
 */
public class ContainerMergeManager {

    // 物品 -> 容器所在背包槽位列表（潜影盒/末影箱/收纳袋自身所在的槽位）
    private Map<ItemStackKey, List<Integer>> containerByItem;

    public ContainerMergeManager() {
        containerByItem = new HashMap<>();
    }

    /**
     * 遍历玩家背包，更新合并映射
     */
    public void updateMapping(PlayerInventory inventory) {
        containerByItem.clear();
        int size = inventory.size();
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (isContainer(stack)) {
                ItemStackKey key = new ItemStackKey(stack);
                containerByItem.computeIfAbsent(key, k -> new ArrayList<>()).add(slot);
            }
        }
    }

    /**
     * 将物品放入容器，优先已有同种物品的容器，否则空容器。
     */
    public void insertItem(PlayerInventory inventory, ItemStack item) {
        if (item.isEmpty() || !isContainer(item)) return;

        List<Integer> containerSlots = containerByItem.getOrDefault(new ItemStackKey(item), new ArrayList<>());
        if (!containerSlots.isEmpty()) {
            // 尝试放入已有同种物品的容器
            for (int slot : containerSlots) {
                ItemStack container = inventory.getStack(slot);
                // 这里简化：直接调用容器的尝试放入逻辑（实际需递归打开容器）
                // 由于不模拟容器内部，跳过实现
                return;
            }
        }
        // 放入第一个空容器（非锁定）
        int emptySlot = findEmptyContainerSlot(inventory);
        if (emptySlot != -1) {
            // 实际需将物品放入该容器内部，这里仅示意
        }
    }

    /**
     * 从容器中取出指定物品
     */
    public void extractItem(PlayerInventory inventory, ItemStack item, int count) {
        // 根据映射找到含有该物品的容器，从中取出
        ItemStackKey key = new ItemStackKey(item);
        List<Integer> slots = containerByItem.get(key);
        if (slots == null) return;
        for (int slot : slots) {
            // 从容器内部取出，此处不实现细节
            break;
        }
    }

    /**
     * 跨容器整理：重排分散物品，考虑锁定槽位
     */
    public void reorganize(PlayerInventory inventory) {
        // 简化：将所有容器内的同种物品集中到一个容器中
        // 需要处理锁定槽位（跳过锁定的容器槽）
        Set<Integer> lockedSlots = getLockedSlots();
        // 遍历容器，移动分散物品
        // 此处不实现具体逻辑
    }

    /**
     * 查询锁定槽位。当前无外部API，返回空集（需另行集成实际API）
     */
    public Set<Integer> getLockedSlots() {
        // 模拟 InvProfilesNext API，实际应调用对应模组
        return new HashSet<>();
    }

    /**
     * 模拟 ItemScroller 滚轮移动（将物品在容器和背包间移动）
     */
    public void simulateScrollMove(PlayerInventory inventory, int fromSlot, int toSlot) {
        // 具体移动逻辑，此处留空
    }

    // --- 内部辅助方法 ---

    private boolean isContainer(ItemStack stack) {
        // 潜影盒、末影箱、收纳袋（Bundle）
        return stack.getItem() == Items.SHULKER_BOX
                || stack.getItem() == Items.ENDER_CHEST
                || stack.getItem() == Items.BUNDLE;
    }

    private int findEmptyContainerSlot(PlayerInventory inventory) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (isContainer(stack) && stack.isEmpty()) {
                // 空容器（实际容器内部可能非空，这里仅作为空槽位判断，其实需要检查容器内部）
                return slot;
            }
        }
        return -1;
    }

    /**
     * 用于映射键的包装，基于物品类型和NBT，忽略数量
     */
    private static class ItemStackKey {
        private final ItemStack stack;

        ItemStackKey(ItemStack stack) {
            this.stack = stack.copy();
            this.stack.setCount(1); // 忽略数量
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemStackKey that = (ItemStackKey) o;
            return ItemStack.areEqual(this.stack, that.stack);
        }

        @Override
        public int hashCode() {
            // 根据物品和NBT计算哈希
            return stack.getItem().hashCode() * 31 + (stack.getNbt() != null ? stack.getNbt().hashCode() : 0);
        }
    }
}