package com.tahai.rootcoinplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TradeCommand implements CommandExecutor, TabCompleter {

    private final List<TradeRequest> pending = new ArrayList<>();

    private static class TradeRequest {
        UUID senderId;
        UUID targetId;
        String senderName;
        String targetName;
        double price;

        TradeRequest(UUID senderId, UUID targetId, String senderName, String targetName, double price) {
            this.senderId = senderId;
            this.targetId = targetId;
            this.senderName = senderName;
            this.targetName = targetName;
            this.price = price;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家使用。");
            return true;
        }
        if (!sender.hasPermission("rootcoin.trade")) {
            player.sendMessage(ChatColor.AQUA + "你没有权限使用交易命令。");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.GRAY + "用法: /trade send <玩家> <金额> 或 /trade accept [玩家]");
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("send")) {
            handleSend(player, args);
        } else if (sub.equals("accept")) {
            handleAccept(player, args);
        } else {
            player.sendMessage(ChatColor.GRAY + "未知子命令。");
        }
        return true;
    }

    private void handleSend(Player sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.GRAY + "用法: /trade send <玩家> <金额>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.AQUA + "玩家 " + args[1] + " 不在线。");
            return;
        }
        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.AQUA + "不能向自己发起交易。");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.AQUA + "金额无效。");
            return;
        }
        if (!Double.isFinite(price) || price <= 0) {
            sender.sendMessage(ChatColor.AQUA + "金额必须大于 0。");
            return;
        }
        ItemStack senderItem = sender.getInventory().getItemInMainHand().clone();
        ItemStack targetItem = target.getInventory().getItemInMainHand().clone();
        if (senderItem.getType() == Material.AIR || targetItem.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.AQUA + "交易双方必须手持一个物品。");
            return;
        }
        if (DataManager.getBalance(sender.getUniqueId()) < price) {
            sender.sendMessage(ChatColor.AQUA + "你的余额不足。");
            return;
        }
        pending.removeIf(r -> r.senderId.equals(sender.getUniqueId()) && r.targetId.equals(target.getUniqueId()));
        pending.add(new TradeRequest(sender.getUniqueId(), target.getUniqueId(), sender.getName(), target.getName(), price));
        sender.sendMessage(ChatColor.YELLOW + "交易请求已发送给 " + target.getName() + "，支付 " + price + " 根号币。");
        target.sendMessage(ChatColor.GRAY + sender.getName() + " 向你发起交易请求。使用 /trade accept " + sender.getName() + " 接受。");
    }

    private void handleAccept(Player accepter, String[] args) {
        TradeRequest req = null;
        if (args.length >= 2) {
            String name = args[1];
            req = pending.stream()
                    .filter(r -> r.targetId.equals(accepter.getUniqueId()) && r.senderName.equalsIgnoreCase(name))
                    .findFirst().orElse(null);
        } else {
            req = pending.stream()
                    .filter(r -> r.targetId.equals(accepter.getUniqueId()))
                    .findFirst().orElse(null);
        }
        if (req == null) {
            accepter.sendMessage(ChatColor.AQUA + "没有找到对应的交易请求。");
            return;
        }
        Player sender = Bukkit.getPlayer(req.senderId);
        if (sender == null) {
            accepter.sendMessage(ChatColor.AQUA + "发起者已离线，交易取消。");
            pending.remove(req);
            return;
        }
        ItemStack senderItem = sender.getInventory().getItemInMainHand().clone();
        ItemStack accepterItem = accepter.getInventory().getItemInMainHand().clone();
        if (senderItem.getType() == Material.AIR || accepterItem.getType() == Material.AIR) {
            accepter.sendMessage(ChatColor.AQUA + "交易失败：双方必须手持物品。");
            pending.remove(req);
            return;
        }
        if (DataManager.getBalance(sender.getUniqueId()) < req.price) {
            accepter.sendMessage(ChatColor.AQUA + "交易失败：发起方余额不足。");
            pending.remove(req);
            return;
        }
        sender.getInventory().setItemInMainHand(accepterItem);
        accepter.getInventory().setItemInMainHand(senderItem);
        DataManager.addBalance(sender.getUniqueId(), -req.price);
        DataManager.addBalance(accepter.getUniqueId(), req.price);
        pending.remove(req);
        sender.sendMessage(ChatColor.YELLOW + "交易完成！你获得了 " + ChatColor.BOLD + accepterItem.getType().name() + ChatColor.YELLOW + "。");
        accepter.sendMessage(ChatColor.YELLOW + "交易完成！你获得了 " + ChatColor.BOLD + senderItem.getType().name() + ChatColor.YELLOW + "。");
        sender.sendMessage(ChatColor.GRAY + "你支付了 " + req.price + " 根号币。");
        accepter.sendMessage(ChatColor.GRAY + "你收到了 " + req.price + " 根号币。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("send", "accept").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("send")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("accept") && sender instanceof Player p) {
                return pending.stream()
                        .filter(r -> r.targetId.equals(p.getUniqueId()))
                        .map(r -> r.senderName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}