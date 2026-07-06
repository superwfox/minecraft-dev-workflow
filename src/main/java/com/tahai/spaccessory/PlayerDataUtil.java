package com.tahai.spaccessory;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class PlayerDataUtil {

    private PlayerDataUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static NamespacedKey getKey() {
        return new NamespacedKey("spaccessory", "accessory_items");
    }

    public static void saveItemsToPDC(Player player, List<ItemStack> items) {
        if (items == null) {
            items = new ArrayList<>();
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(items.size());
            for (ItemStack item : items) {
                byte[] serialized = item.serializeAsBytes();
                dos.writeInt(serialized.length);
                dos.write(serialized);
            }
            player.getPersistentDataContainer().set(getKey(), PersistentDataType.BYTE_ARRAY, baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ItemStack> loadItemsFromPDC(Player player) {
        byte[] data = player.getPersistentDataContainer().get(getKey(), PersistentDataType.BYTE_ARRAY);
        if (data == null) {
            return new ArrayList<>();
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {
            int count = dis.readInt();
            List<ItemStack> items = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int len = dis.readInt();
                byte[] bytes = new byte[len];
                dis.readFully(bytes);
                ItemStack item = ItemStack.deserializeFromBytes(bytes);
                if (item != null) {
                    items.add(item);
                }
            }
            return items;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}