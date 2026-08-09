package com.tahai.sect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class JoinSectGuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof JoinSectGuiHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getInventory().getSize()) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        JoinSectGuiHolder holder = (JoinSectGuiHolder) event.getInventory().getHolder();

        if (name.contains("上一页")) {
            new JoinSectGuiHolder(holder.getCurrentPage() - 1).open(player);
            return;
        }
        if (name.contains("下一页")) {
            new JoinSectGuiHolder(holder.getCurrentPage() + 1).open(player);
            return;
        }
        if (name.contains("关闭")) {
            player.closeInventory();
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) return;

        GuildManager guildManager = new GuildManager(plugin);
        if (guildManager.guildExists(name)) {
            if (guildManager.applyJoin(player, name)) {
                player.sendMessage(ChatColor.YELLOW + "加入申请已发送给宗主。");
            } else {
                player.sendMessage(ChatColor.AQUA + "无法发送加入申请。");
            }
        }
    }
}