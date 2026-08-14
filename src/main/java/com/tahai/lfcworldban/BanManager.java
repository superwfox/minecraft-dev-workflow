package com.tahai.lfcworldban;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public class BanManager {

    private final Plugin plugin;
    private final Map<String, Set<Material>> bannedItems = new HashMap<>();
    private final Map<UUID, List<ItemStack>> forcedOffItems = new HashMap<>();

    public BanManager(Plugin plugin) {
        this.plugin = plugin;
        loadBannedItems();
    }

    public void reload() {
        plugin.reloadConfig();
        loadBannedItems();
        forcedOffItems.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayer(player);
        }
    }

    public void addBannedItem(String world, Material type) {
        if (type == null || type.isAir()) return;
        String key = world.toLowerCase(Locale.ROOT);
        Set<Material> set = bannedItems.computeIfAbsent(key, k -> new HashSet<>());
        if (!set.add(type)) return;

        List<String> list = new ArrayList<>();
        for (Material material : set) {
            list.add(material.name());
        }
        Collections.sort(list);
        plugin.getConfig().set("banned-items." + key, list);
        save();
    }

    public boolean isBannedItem(World world, ItemStack item) {
        return item != null && !item.getType().isAir() && isBanned(world.getName(), item.getType());
    }

    public void checkPlayer(Player player) {
        PlayerInventory inv = player.getInventory();
        List<ItemStack> removed = new ArrayList<>();

        ItemStack[] armor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (isBannedItem(player.getWorld(), item)) {
                armor[i] = null;
                removed.add(item);
            }
        }
        inv.setArmorContents(armor);

        ItemStack main = inv.getItemInMainHand();
        if (isBannedItem(player.getWorld(), main)) {
            inv.setItemInMainHand(null);
            removed.add(main);
        }

        ItemStack off = inv.getItemInOffHand();
        if (isBannedItem(player.getWorld(), off)) {
            inv.setItemInOffHand(null);
            removed.add(off);
        }

        if (removed.isEmpty()) {
            forcedOffItems.remove(player.getUniqueId());
            return;
        }

        forcedOffItems.put(player.getUniqueId(), removed);

        String message = plugin.getConfig().getString("messages.removed", "");
        for (ItemStack item : removed) {
            forceRemove(player, item);
            if (!message.isEmpty()) {
                sendMessage(player, message.replace("%item%", item.getType().name()));
            }
        }
    }

    public Location forceRemove(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return null;

        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
        if (!left.isEmpty()) {
            left = player.getEnderChest().addItem(left.values().iterator().next());
        }
        if (!left.isEmpty()) {
            ItemStack drop = left.values().iterator().next();
            return player.getWorld().dropItemNaturally(player.getLocation(), drop).getLocation();
        }
        return null;
    }

    public List<ItemStack> getForcedItems(OfflinePlayer player) {
        List<ItemStack> list = forcedOffItems.get(player.getUniqueId());
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
    }

    public void save() {
        plugin.saveConfig();
    }

    public void shutdown() {
        save();
        forcedOffItems.clear();
    }

    private boolean isBanned(String worldName, Material type) {
        Set<Material> set = bannedItems.get(worldName.toLowerCase(Locale.ROOT));
        return set != null && set.contains(type);
    }

    private void loadBannedItems() {
        bannedItems.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("banned-items");
        if (section == null) return;

        for (String world : section.getKeys(false)) {
            Set<Material> set = new HashSet<>();
            for (String name : section.getStringList(world)) {
                Material material = Material.matchMaterial(name);
                if (material != null) {
                    set.add(material);
                }
            }
            if (!set.isEmpty()) {
                bannedItems.put(world.toLowerCase(Locale.ROOT), set);
            }
        }
    }

    private void sendMessage(Player player, String raw) {
        if (raw == null || raw.isEmpty()) return;
        player.sendMessage(MiniMessage.miniMessage().deserialize(legacyToMiniMessage(raw)));
    }

    private String legacyToMiniMessage(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == '#') {
                    if (i + 8 <= text.length() && text.substring(i + 2, i + 8).matches("[0-9a-fA-F]{6}")) {
                        sb.append("<#").append(text, i + 2, i + 8).append('>');
                        i += 7;
                        continue;
                    }
                } else {
                    String tag = legacyColorTag(next);
                    if (tag != null) {
                        sb.append('<').append(tag).append('>');
                        i++;
                        continue;
                    }
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private String legacyColorTag(char c) {
        switch (Character.toLowerCase(c)) {
            case '0': return "black";
            case '1': return "dark_blue";
            case '2': return "dark_green";
            case '3': return "dark_aqua";
            case '4': return "dark_red";
            case '5': return "dark_purple";
            case '6': return "gold";
            case '7': return "gray";
            case '8': return "dark_gray";
            case '9': return "blue";
            case 'a': return "green";
            case 'b': return "aqua";
            case 'c': return "red";
            case 'd': return "light_purple";
            case 'e': return "yellow";
            case 'f': return "white";
            case 'k': return "obfuscated";
            case 'l': return "bold";
            case 'm': return "strikethrough";
            case 'n': return "underlined";
            case 'o': return "italic";
            case 'r': return "reset";
            default: return null;
        }
    }
}