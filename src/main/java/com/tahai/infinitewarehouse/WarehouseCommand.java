package com.tahai.infinitewarehouse;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class WarehouseCommand implements CommandExecutor, TabCompleter {

    private static final String NBT_KEY = "infinitewarehouse";
    private static final String NBT_VALUE = "true";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "Only players can use this command.");
            return true;
        }
        if (!sender.hasPermission("warehouse.get")) {
            sender.sendMessage(ChatColor.AQUA + "You don't have permission to use this command.");
            return true;
        }
        if (args.length < 1 || !args[0].equalsIgnoreCase("get")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /warehouse get");
            return true;
        }

        Player player = (Player) sender;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("InfiniteWarehouse");
        if (plugin == null) {
            player.sendMessage(ChatColor.AQUA + "Plugin not found.");
            return true;
        }

        String materialName = plugin.getConfig().getString("warehouse.item.material", "DIAMOND");
        String displayName = plugin.getConfig().getString("warehouse.item.name", "Infinite Warehouse Item");
        List<String> lore = plugin.getConfig().getStringList("warehouse.item.lore");
        if (lore.isEmpty()) {
            lore = Collections.singletonList(ChatColor.GRAY + "A special warehouse item.");
        } else {
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
        }

        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            material = Material.DIAMOND;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        item = setNBT(item);

        if (player.getInventory().addItem(item).isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You received the special item.");
        } else {
            player.getWorld().dropItem(player.getLocation(), item);
            player.sendMessage(ChatColor.YELLOW + "Your inventory was full, item dropped.");
        }
        return true;
    }

    private ItemStack setNBT(ItemStack item) {
        try {
            Class<?> craftItemStackClass = getCraftClass("inventory.CraftItemStack");
            Class<?> nmsItemStackClass = getNMSClass("ItemStack");
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");

            Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItem = asNMSCopy.invoke(null, item);

            Object tag = nbtTagCompoundClass.newInstance();
            Method setString = nbtTagCompoundClass.getMethod("setString", String.class, String.class);
            setString.invoke(tag, NBT_KEY, NBT_VALUE);

            Method setTagMethod = nmsItemStackClass.getMethod("setTag", nbtTagCompoundClass);
            setTagMethod.invoke(nmsItem, tag);

            Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            return (ItemStack) asBukkitCopy.invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
            return item;
        }
    }

    private Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getPackageVersion() + "." + name);
    }

    private Class<?> getCraftClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getPackageVersion() + "." + name);
    }

    private String getPackageVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("get");
        }
        return Collections.emptyList();
    }
}