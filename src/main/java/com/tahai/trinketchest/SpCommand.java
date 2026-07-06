package com.tahai.trinketchest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpCommand implements CommandExecutor, TabCompleter {

    private static final Pattern PERM_PATTERN = Pattern.compile("^sp\\.chest\\.(\\d+)$");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行。");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("sp.chest.use")) {
            player.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
            return true;
        }

        int sum = 0;
        for (org.bukkit.permissions.PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            Matcher matcher = PERM_PATTERN.matcher(info.getPermission());
            if (matcher.matches()) {
                try {
                    sum += Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (sum <= 0) {
            player.sendMessage(ChatColor.RED + "你没有任何饰品栏位！");
            return true;
        }

        GUIHolder holder = new GUIHolder();
        List<ItemStack> existingItems = holder.loadItemsFromPDC(player);
        if (existingItems == null) {
            existingItems = new ArrayList<>();
        }
        holder.saveItemsToPDC(player, existingItems, sum);
        holder.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}