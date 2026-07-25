package com.tahai.baoshi;

import com.tahai.baoshi.GemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GiveCommand implements CommandExecutor, TabCompleter {

    private static final List<String> TYPES = Arrays.asList("青鳞石", "落凤石", "玄冰石", "灵犀石", "粘合剂");
    private static final List<String> LEVELS = Arrays.asList("1", "2", "3", "4", "5");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("baoshi.admin")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.GRAY + "用法: /baoshi give <玩家> <类型> <等级>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.AQUA + "玩家不在线。");
            return true;
        }

        String type = args[1];
        if (!TYPES.contains(type)) {
            sender.sendMessage(ChatColor.AQUA + "无效的类型。可用类型: " + String.join(", ", TYPES));
            return true;
        }

        ItemStack item;
        if (type.equals("粘合剂")) {
            item = GemBuilder.createGlue();
        } else {
            int level;
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.AQUA + "等级必须是数字。");
                return true;
            }
            if (level < 1 || level > 5) {
                sender.sendMessage(ChatColor.AQUA + "等级必须在1到5之间。");
                return true;
            }
            item = GemBuilder.createGem(type, level);
        }

        target.getInventory().addItem(item).values().forEach(leftover ->
            target.getWorld().dropItemNaturally(target.getLocation(), leftover)
        );

        sender.sendMessage(ChatColor.YELLOW + "成功给予 " + target.getName() + " 一个" + type + 
            (type.equals("粘合剂") ? "" : " (等级" + args[2] + ")") + "。");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("baoshi.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            String prefix = args[0].toLowerCase();
            return playerNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            return TYPES.stream()
                    .filter(t -> t.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            // 只有非粘合剂才补全等级
            if (args[1].equals("粘合剂")) {
                return Collections.emptyList();
            }
            String prefix = args[2];
            return LEVELS.stream()
                    .filter(l -> l.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}