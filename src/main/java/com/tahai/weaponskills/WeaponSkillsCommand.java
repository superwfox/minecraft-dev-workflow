package com.tahai.weaponskills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WeaponSkillsCommand implements CommandExecutor, TabCompleter {

    private static final List<String> WEAPON_TYPES = Arrays.asList("剑", "斧", "三叉戟", "弩", "重锤");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "只有玩家才能使用此命令");
            return true;
        }

        if (!sender.hasPermission("weaponskills.command.use")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限使用此命令");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.GRAY + "用法: /" + label + " <武器种类>");
            return true;
        }

        String skill = args[0];
        if (!WEAPON_TYPES.contains(skill)) {
            sender.sendMessage(ChatColor.AQUA + "无效武器种类，可选：剑, 斧, 三叉戟, 弩, 重锤");
            return true;
        }

        Player player = (Player) sender;

        Main plugin = (Main) Bukkit.getPluginManager().getPlugin("WeaponSkills");
        if (plugin == null) {
            player.sendMessage(ChatColor.AQUA + "插件未加载");
            return true;
        }

        DataManager dataManager = plugin.getDataManager();
        dataManager.setSkill(player.getUniqueId(), skill);
        dataManager.save();

        player.sendMessage(ChatColor.YELLOW + "技能已设置为: " + skill);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String type : WEAPON_TYPES) {
                if (type.toLowerCase().startsWith(partial)) {
                    completions.add(type);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}