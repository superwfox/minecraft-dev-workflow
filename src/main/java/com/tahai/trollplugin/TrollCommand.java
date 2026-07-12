package com.tahai.trollplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TrollCommand implements TabExecutor {

    private static final String[] EFFECTS = {
            "reversecontrols", "fakebossbar", "gravityreverse", "randomweather",
            "explosion", "mobinvasion", "sound", "creeperstalker", "villagertrial",
            "animaltalk", "snowmanclone", "forceclearinventory", "catcannon", "dirtrain"
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("joke.use")) {
            sender.sendMessage(Component.text("你没有权限执行此命令！").color(NamedTextColor.RED));
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("TrollPlugin");
        if (plugin == null) {
            sender.sendMessage(Component.text("插件未加载！").color(NamedTextColor.RED));
            return true;
        }

        com.tahai.trollplugin.Main mainPlugin = (com.tahai.trollplugin.Main) plugin;
        PrankManager prankManager = mainPlugin.getPrankManager();

        if (args.length == 0) {
            Component menu = Component.text("----- Troll 恶作剧菜单 -----\n").color(NamedTextColor.GOLD)
                    .append(Component.text("[选择玩家并应用恶作剧]\n").color(NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.suggestCommand("/troll start ")))
                    .append(Component.text("[停止某玩家的所有恶作剧]\n").color(NamedTextColor.YELLOW)
                            .clickEvent(ClickEvent.suggestCommand("/troll stop ")))
                    .append(Component.text("[停止所有恶作剧]\n").color(NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/troll stopall")));
            sender.sendMessage(menu);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("用法: /troll start <效果> <玩家>").color(NamedTextColor.RED));
                    return true;
                }
                String effect = args[1].toLowerCase();
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage(Component.text("玩家 " + args[2] + " 不在线！").color(NamedTextColor.RED));
                    return true;
                }
                applyEffect(prankManager, target, effect);
                sender.sendMessage(Component.text("已对 " + target.getName() + " 应用恶作剧: " + effect).color(NamedTextColor.GREEN));
                break;
            case "stop":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("用法: /troll stop <玩家>").color(NamedTextColor.RED));
                    return true;
                }
                Player stopTarget = Bukkit.getPlayerExact(args[1]);
                if (stopTarget == null) {
                    sender.sendMessage(Component.text("玩家 " + args[1] + " 不在线！").color(NamedTextColor.RED));
                    return true;
                }
                prankManager.stopPlayerAllPranks(stopTarget);
                sender.sendMessage(Component.text("已停止 " + stopTarget.getName() + " 的所有恶作剧").color(NamedTextColor.GREEN));
                break;
            case "stopall":
                prankManager.stopAllPranks();
                sender.sendMessage(Component.text("已停止所有恶作剧").color(NamedTextColor.GREEN));
                break;
            default:
                sender.sendMessage(Component.text("未知子命令！可用: start, stop, stopall").color(NamedTextColor.RED));
                break;
        }
        return true;
    }

    private void applyEffect(PrankManager manager, Player target, String effect) {
        switch (effect) {
            case "reversecontrols": manager.startReverseControls(target); break;
            case "fakebossbar": manager.startFakeBossBar(target); break;
            case "gravityreverse": manager.startGravityReverse(target); break;
            case "randomweather": manager.startRandomWeather(target); break;
            case "explosion": manager.startExplosion(target); break;
            case "mobinvasion": manager.startMobInvasion(target); break;
            case "sound": manager.startSound(target); break;
            case "creeperstalker": manager.startCreeperStalker(target); break;
            case "villagertrial": manager.startVillagerTrial(target); break;
            case "animaltalk": manager.startAnimalTalk(target); break;
            case "snowmanclone": manager.startSnowmanClone(target); break;
            case "forceclearinventory": manager.startForceClearInventory(target); break;
            case "catcannon": manager.startCatCannon(target); break;
            case "dirtrain": manager.startDirtRain(target); break;
            default:
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("joke.use")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("start", "stop", "stopall");
        }

        if (args.length == 2) {
            String first = args[0].toLowerCase();
            if ("start".equals(first)) {
                List<String> matches = new ArrayList<>();
                for (String e : EFFECTS) {
                    if (e.startsWith(args[1].toLowerCase())) {
                        matches.add(e);
                    }
                }
                return matches;
            }
            if ("stop".equals(first)) {
                return null;
            }
            return Collections.emptyList();
        }

        if (args.length == 3 && "start".equals(args[0].toLowerCase())) {
            return null;
        }

        return Collections.emptyList();
    }
}