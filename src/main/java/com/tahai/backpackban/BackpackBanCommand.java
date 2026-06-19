package com.tahai.backpackban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BackpackBanCommand implements CommandExecutor, TabCompleter {

    private File stateFile;

    public BackpackBanCommand() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BackpackBan");
        if (plugin != null && plugin.getDataFolder().exists()) {
            stateFile = new File(plugin.getDataFolder(), "state.txt");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("backpackban.command")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "控制台不能使用此命令！");
            return true;
        }

        if (args.length != 1 || (!args[0].equalsIgnoreCase("enable") && !args[0].equalsIgnoreCase("disable"))) {
            sender.sendMessage(ChatColor.RED + "用法: /backpackban <enable|disable>");
            return true;
        }

        boolean enable = args[0].equalsIgnoreCase("enable");
        setState(enable);

        if (enable) {
            // 清空所有在线非OP玩家的背包（保留0-8和副手）
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) continue;
                clearBackpackExceptHotbarAndOffhand(player);
            }
            sender.sendMessage(ChatColor.GREEN + "已启用背包禁令");
        } else {
            sender.sendMessage(ChatColor.GREEN + "已禁用背包禁令");
        }
        return true;
    }

    private void clearBackpackExceptHotbarAndOffhand(Player player) {
        // 槽位 0-8 热键栏，9-35 主背包，36-39 盔甲，40 副手
        // 保留0-8和40，其余全部清空
        for (int slot = 9; slot <= 39; slot++) {
            player.getInventory().setItem(slot, null);
        }
        // 盔甲槽39？实际上盔甲槽是36-39，已经包含在9-39中（9-35主背包，36-39盔甲）
        // 注意：39是靴子，也清除
    }

    private void setState(boolean enable) {
        try {
            if (stateFile == null) return;
            if (!stateFile.getParentFile().exists()) stateFile.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(stateFile))) {
                writer.write(enable ? "1" : "0");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isEnabled() {
        if (stateFile == null || !stateFile.exists()) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(stateFile))) {
            String line = reader.readLine();
            return "1".equals(line);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("enable", "disable");
        }
        return Collections.emptyList();
    }
}