package com.tahai.buildershield;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SetShieldItemCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BuilderShield");
        if (!sender.isOp() && !sender.hasPermission("builder.shield.admin")) {
            String msg = plugin.getConfig().getString("messages.no-permission", ChatColor.AQUA + "你没有权限执行此命令");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(ChatColor.GRAY + "用法: /buildershield give <玩家>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.AQUA + "玩家 " + args[1] + " 不在线");
            return true;
        }
        String materialName = plugin.getConfig().getString("items.shield.material", "STICK");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.STICK;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String name = plugin.getConfig().getString("items.shield.name", "建造者护盾");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> lore = plugin.getConfig().getStringList("items.shield.lore");
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        if (!coloredLore.isEmpty()) {
            meta.setLore(coloredLore);
        }
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "shield_item"), PersistentDataType.STRING, "true");
        item.setItemMeta(meta);
        Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), drop);
            }
        }
        sender.sendMessage(ChatColor.YELLOW + "已将建造者护盾给予 " + target.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if ("give".startsWith(args[0].toLowerCase())) {
                return Collections.singletonList("give");
            }
            return Collections.emptyList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> names = new ArrayList<>();
            String prefix = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    names.add(player.getName());
                }
            }
            return names;
        }
        return Collections.emptyList();
    }
}