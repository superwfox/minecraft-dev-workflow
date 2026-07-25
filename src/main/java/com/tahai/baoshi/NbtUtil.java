package com.tahai.baoshi;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.UUID;

public final class NbtUtil {

    private static final String CRAFTBUKKIT_INVENTORY = "org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack";
    private static final String NMS_ITEM_STACK = "net.minecraft.server.v1_12_R1.ItemStack";
    private static final String NMS_NBT_TAG_COMPOUND = "net.minecraft.server.v1_12_R1.NBTTagCompound";

    private static Method craftAsNMSCopy;
    private static Method craftAsBukkitCopy;
    private static Method nmsGetTag;
    private static Method nmsSetTag;
    private static Method nbtHasKey;
    private static Method nbtSetString;
    private static Method nbtGetString;

    static {
        try {
            Class<?> craftItemStackClass = Class.forName(CRAFTBUKKIT_INVENTORY);
            craftAsNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Class<?> nmsItemStackClass = Class.forName(NMS_ITEM_STACK);
            craftAsBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            nmsGetTag = nmsItemStackClass.getMethod("getTag");
            nmsSetTag = nmsItemStackClass.getMethod("setTag", Class.forName(NMS_NBT_TAG_COMPOUND));
            Class<?> nmsNbtTagCompoundClass = Class.forName(NMS_NBT_TAG_COMPOUND);
            nbtHasKey = nmsNbtTagCompoundClass.getMethod("hasKey", String.class);
            nbtSetString = nmsNbtTagCompoundClass.getMethod("setString", String.class, String.class);
            nbtGetString = nmsNbtTagCompoundClass.getMethod("getString", String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize NBT reflection helper", e);
        }
    }

    private NbtUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack setUUID(ItemStack item, UUID uuid) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) {
            return item;
        }
        try {
            Object nmsStack = craftAsNMSCopy.invoke(null, item);
            Object tag = nmsGetTag.invoke(nmsStack);
            if (tag == null) {
                tag = Class.forName(NMS_NBT_TAG_COMPOUND).newInstance();
            }
            nbtSetString.invoke(tag, "baoshi_uuid", uuid.toString());
            nmsSetTag.invoke(nmsStack, tag);
            return (ItemStack) craftAsBukkitCopy.invoke(null, nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
            return item;
        }
    }

    public static UUID getUUID(ItemStack item) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) {
            return null;
        }
        try {
            Object nmsStack = craftAsNMSCopy.invoke(null, item);
            Object tag = nmsGetTag.invoke(nmsStack);
            if (tag == null || !(Boolean) nbtHasKey.invoke(tag, "baoshi_uuid")) {
                return null;
            }
            String raw = (String) nbtGetString.invoke(tag, "baoshi_uuid");
            return UUID.fromString(raw);
        } catch (Exception e) {
            return null;
        }
    }
}