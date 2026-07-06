package com.tahai.spaccessory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class SpCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "这条命令只能由玩家执行");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("sp.command")) {
            player.sendMessage(ChatColor.RED + "你没有权限使用此命令");
            return true;
        }

        // 计算饰品栏位数
        int totalSlots = 0;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String perm = info.getPermission();
            if (perm.startsWith("sp.chest.")) {
                try {
                    int slot = Integer.parseInt(perm.substring("sp.chest.".length()));
                    totalSlots += slot;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 默认至少6格
        int accessibleSlots = Math.max(totalSlots, 6);

        // 确保箱子大小是9的倍数且不超过54
        int size = ((accessibleSlots + 8) / 9) * 9;
        if (size > 54) size = 54;

        // 创建 Inventory
        Inventory inv = GuiHolder.createInventory(player, size);
        GuiHolder holder = (GuiHolder) inv.getHolder();

        // 设置 enabled 槽位
        for (int i = 0; i < size; i++) {
            holder.setSlotEnabled(i, i < accessibleSlots);
        }

        // 填充禁用槽位占位物品
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName("§c§l✘");
        placeholder.setItemMeta(meta);
        for (int i = accessibleSlots; i < size; i++) {
            inv.setItem(i, placeholder.clone());
        }

        // 读取并放置饰品物品
        List<ItemStack> storedItems = PlayerDataUtil.loadItemsFromPDC(player);
        int limit = Math.min(storedItems.size(), accessibleSlots);
        for (int i = 0; i < limit; i++) {
            ItemStack item = storedItems.get(i);
            if (item != null && !item.getType().isAir()) {
                inv.setItem(i, item);
            }
        }

        player.openInventory(inv);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}