package com.tahai.funtroll;

import com.tahai.funtroll.TrollGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrollGUIListener implements Listener {

    private final Map<UUID, TrollGUI> openGUIs = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder().getClass().getName().equals("com.tahai.funtroll.TrollGUI"))) return;
        if (event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);
            Object gui = event.getView().getTopInventory().getHolder();
            Inventory inv = event.getView().getTopInventory();
            String title = inv.getTitle();
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= inv.getSize()) return;

            if (title.equals(ChatColor.GOLD + "Troll Menu")) {
                handleMainMenuClick(player, gui, slot);
            } else if (title.startsWith(ChatColor.BLUE + "Select Player")) {
                handlePlayerListClick(player, gui, inv, slot);
            } else if (title.startsWith(ChatColor.RED + "Troll: ")) {
                handleTrollMenuClick(player, inv, title, slot);
            }
        }
    }

    private void handleMainMenuClick(Player player, Object gui, int slot) {
        switch (slot) {
            case 0:
                try {
                    gui.getClass().getMethod("openPlayerList", Player.class).invoke(gui, player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open player list.");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown option.");
        }
    }

    private void handlePlayerListClick(Player player, Object gui, Inventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) return;
        Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player is offline.");
            return;
        }
        try {
            gui.getClass().getMethod("openPlayerTrollMenu", Player.class, Player.class).invoke(gui, player, target);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to open troll menu.");
        }
    }

    private void handleTrollMenuClick(Player player, Inventory inv, String title, int slot) {
        String targetName = title.substring((ChatColor.RED + "Troll: " + ChatColor.GREEN).length()).trim();
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Target player is offline.");
            return;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("FunTroll");
        if (plugin == null) return;
        TrollManager tm = null;
        try {
            java.lang.reflect.Field field = plugin.getClass().getDeclaredField("trollManager");
            field.setAccessible(true);
            tm = (TrollManager) field.get(plugin);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Internal error.");
            return;
        }
        if (tm == null) return;

        switch (slot) {
            case 0:
                tm.startTroll(target, "freeze");
                player.sendMessage(ChatColor.GREEN + "Frozen " + target.getName());
                break;
            case 1:
                tm.startTroll(target, "anvil");
                player.sendMessage(ChatColor.GREEN + "Anvil dropped on " + target.getName());
                break;
            case 2:
                tm.startTroll(target, "lightning");
                player.sendMessage(ChatColor.GREEN + "Lightning struck " + target.getName());
                break;
            case 3:
                tm.stopAllTrolls(target);
                player.sendMessage(ChatColor.GREEN + "Stopped all trolls on " + target.getName());
                break;
            case 4:
                tm.stopAllTrolls();
                player.sendMessage(ChatColor.GREEN + "Stopped all trolls on all players");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown option.");
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (event.getInventory().getHolder() instanceof TrollGUI) {
                openGUIs.remove(player.getUniqueId());
            }
        }
    }

    public void registerOpenGUI(Player player, TrollGUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
    }
}