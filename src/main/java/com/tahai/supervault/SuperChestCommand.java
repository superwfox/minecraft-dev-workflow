package com.tahai.supervault;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class SuperChestCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("superchest.give")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令！");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;
        ItemStack key = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "超级仓库");
        key.setItemMeta(meta);
        player.getInventory().addItem(key);
        player.sendMessage(ChatColor.YELLOW + "已给予你一个" + ChatColor.GOLD + "超级仓库" + ChatColor.YELLOW + "钥匙！");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}