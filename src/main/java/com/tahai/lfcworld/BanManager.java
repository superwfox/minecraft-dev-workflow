package com.tahai.lfcworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanManager {
    private final Map<String, List<String>> bannedItems = new HashMap<>();
    private boolean promptEnabled;
    private String promptMessage;

    public BanManager() {
        reload();
    }

    public void reload() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LfcWorld");
        if (plugin == null) return;
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        bannedItems.clear();
        ConfigurationSection section = config.getConfigurationSection("world-bans");
        if (section != null) {
            for (String world : section.getKeys(false)) {
                bannedItems.put(world, new ArrayList<>(config.getStringList("world-bans." + world)));
            }
        }
        promptEnabled = config.getBoolean("prompt.enabled", true);
        promptMessage = config.getString("prompt.message", "");
    }

    public boolean isWorldBannedItem(String worldName, Material itemType) {
        List<String> items = bannedItems.get(worldName);
        return items != null && items.contains(itemType.name());
    }

    public PromptConfig getPromptConfig() {
        return new PromptConfig(promptEnabled, promptMessage);
    }

    public void addBan(String worldName, String itemId) {
        bannedItems.computeIfAbsent(worldName, k -> new ArrayList<>()).add(itemId);
        saveConfig();
    }

    public void save() {
        saveConfig();
    }

    public void shutdown() {
        saveConfig();
    }

    public void handleBannedItems(Player player, String worldName) {
        PlayerInventory inv = player.getInventory();
        List<ItemStack> banned = new ArrayList<>();
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && isWorldBannedItem(worldName, item.getType())) {
                banned.add(item);
                contents[i] = null;
            }
        }
        inv.setContents(contents);

        if (banned.isEmpty()) return;

        for (ItemStack item : banned) {
            Map<Integer, ItemStack> leftover = inv.addItem(item);
            if (!leftover.isEmpty()) {
                Map<Integer, ItemStack> leftover2 = player.getEnderChest().addItem(leftover.values().toArray(new ItemStack[0]));
                if (!leftover2.isEmpty()) {
                    for (ItemStack drop : leftover2.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
            }
        }

        PromptConfig pc = getPromptConfig();
        if (pc.isEnabled() && !pc.getMessage().isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', pc.getMessage()));
        }
    }

    private void saveConfig() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LfcWorld");
        if (plugin == null) return;
        FileConfiguration config = plugin.getConfig();
        for (Map.Entry<String, List<String>> entry : bannedItems.entrySet()) {
            config.set("world-bans." + entry.getKey(), entry.getValue());
        }
        plugin.saveConfig();
    }

    public static class PromptConfig {
        private final boolean enabled;
        private final String message;

        public PromptConfig(boolean enabled, String message) {
            this.enabled = enabled;
            this.message = message;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getMessage() {
            return message;
        }
    }
}