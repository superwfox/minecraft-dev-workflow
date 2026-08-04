package com.tahai.pvpduel;

import java.util.Collections;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class AcceptCommand implements CommandExecutor, TabCompleter {

    private final DuelManager duelManager;

    public AcceptCommand(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("pvpduel.accept")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限执行此命令");
            return true;
        }

        DuelInvite invite = duelManager.accept(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(ChatColor.AQUA + "你没有待接受的决斗邀请");
            return true;
        }

        Player inviter = Bukkit.getPlayer(invite.getInviter());
        if (inviter == null || !inviter.isOnline()) {
            player.sendMessage(ChatColor.AQUA + "发起者已不在线");
            return true;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null || rsp.getProvider() == null) {
            player.sendMessage(ChatColor.AQUA + "经济系统不可用");
            return true;
        }

        Economy economy = rsp.getProvider();
        if (economy.getBalance(player) < 5000.0) {
            player.sendMessage(ChatColor.AQUA + "你的余额不足 5000");
            return true;
        }

        player.teleport(inviter.getLocation());
        duelManager.removeInvite(player.getUniqueId());

        player.sendMessage(ChatColor.YELLOW + "你已接受决斗邀请，决斗开始！");
        inviter.sendMessage(ChatColor.YELLOW + player.getName() + " 接受了你的决斗邀请！");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}