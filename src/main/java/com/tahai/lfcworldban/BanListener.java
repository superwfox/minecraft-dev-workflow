package com.tahai.lfcworldban;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public class BanListener implements Listener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final BanManager banManager;
    private final Plugin plugin;

    public BanListener(BanManager banManager) {
        this.banManager = banManager;
        this.plugin = Bukkit.getPluginManager().getPlugin("LFCWorldBan");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getClickedInventory() instanceof PlayerInventory)) return;

        boolean equipping = false;
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        int slot = event.getSlot();

        if (event.getSlotType() == InventoryType.SlotType.ARMOR || slot == 40) {
            if (cursor != null && cursor.getType() != Material.AIR
                    && banManager.isBannedItem(player.getWorld(), cursor)) {
                equipping = true;
            }
        }

        if (event.isShiftClick() && slot < 36
                && event.getView().getTopInventory() instanceof PlayerInventory
                && item != null && isEquippable(item)
                && banManager.isBannedItem(player.getWorld(), item)) {
            equipping = true;
        }

        if (equipping) {
            event.setCancelled(true);
            sendBlockEquip(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !isEquippable(item)) return;
        if (!banManager.isBannedItem(player.getWorld(), item)) return;
        event.setCancelled(true);
        sendBlockEquip(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        banManager.checkPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        banManager.checkPlayer(event.getPlayer());
    }

    private boolean isEquippable(ItemStack item) {
        Material type = item.getType();
        if (type == Material.ELYTRA || type == Material.SHIELD || type == Material.CARVED_PUMPKIN) return true;
        String name = type.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || name.endsWith("_HEAD") || name.endsWith("_SKULL");
    }

    private void sendBlockEquip(Player player) {
        if (plugin == null) return;
        String message = plugin.getConfig().getString("messages.block_equip",
                ChatColor.AQUA + "You cannot equip banned items in this world!");
        if (message == null) return;
        player.sendMessage(parse(message));
    }

    private Component parse(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '&' || c == '\u00A7') && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                String tag = switch (code) {
                    case '0' -> "black";
                    case '1' -> "dark_blue";
                    case '2' -> "dark_green";
                    case '3' -> "dark_aqua";
                    case '4' -> "dark_red";
                    case '5' -> "dark_purple";
                    case '6' -> "gold";
                    case '7' -> "gray";
                    case '8' -> "dark_gray";
                    case '9' -> "blue";
                    case 'a' -> "green";
                    case 'b' -> "aqua";
                    case 'c' -> "red";
                    case 'd' -> "light_purple";
                    case 'e' -> "yellow";
                    case 'f' -> "white";
                    case 'k' -> "obfuscated";
                    case 'l' -> "bold";
                    case 'm' -> "strikethrough";
                    case 'n' -> "underline";
                    case 'o' -> "italic";
                    case 'r' -> "reset";
                    default -> null;
                };
                if (tag != null) {
                    sb.append('<').append(tag).append('>');
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        try {
            return MINI_MESSAGE.deserialize(sb.toString());
        } catch (RuntimeException ex) {
            return Component.text(text);
        }
    }
}