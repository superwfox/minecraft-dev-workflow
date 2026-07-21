package com.tahai.lottery;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class GUIListener implements Listener {

    private final DataManager dataManager;
    private final Plugin plugin;
    private final Map<UUID, String[]> pendingInput = new HashMap<>();

    public GUIListener(DataManager dataManager) {
        if (dataManager == null) {
            throw new IllegalArgumentException("DataManager cannot be null");
        }
        this.dataManager = dataManager;
        this.plugin = dataManager.getPlugin();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        String adminTitle = dataManager.getConfigMessage("admin-gui-title");
        String editTitle = dataManager.getConfigMessage("edit-gui-title");
        String selectTitle = dataManager.getConfigMessage("box-select-gui-title");

        if (title.equals(adminTitle)) {
            handleAdminClick(player, inv, current, event.getRawSlot());
        } else if (title.equals(editTitle)) {
            handleEditClick(player, inv, current, event.getRawSlot());
        } else if (title.equals(selectTitle)) {
            handleSelectClick(player, inv, current);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!pendingInput.containsKey(uuid)) return;
        String[] data = pendingInput.get(uuid);
        String type = data[0];
        event.setCancelled(true);
        pendingInput.remove(uuid);

        if (type.equals("create")) {
            String boxId = event.getMessage().trim();
            if (boxId.isEmpty()) {
                player.sendMessage(dataManager.getConfigMessage("invalid-key-message"));
                return;
            }
            if (dataManager.getBox(boxId) != null) {
                player.sendMessage(dataManager.getConfigMessage("chest-registered"));
                return;
            }
            dataManager.addBox(boxId, new HashMap<>(), new HashMap<>());
            player.sendMessage(dataManager.getConfigMessage("operation-success"));
        } else if (type.equals("key")) {
            String[] parts = event.getMessage().split("\\s+");
            if (parts.length != 2) {
                player.sendMessage(dataManager.getConfigMessage("invalid-key-message"));
                return;
            }
            String boxId = parts[0];
            int amount;
            try {
                amount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(dataManager.getConfigMessage("invalid-key-message"));
                return;
            }
            if (dataManager.getBox(boxId) == null) {
                player.sendMessage(dataManager.getConfigMessage("chest-not-registered"));
                return;
            }
            ItemStack key = new ItemStack(Material.PAPER);
            ItemMeta meta = key.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "🔑 " + boxId + " 钥匙");
            key.setItemMeta(meta);
            key.setAmount(amount);
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(key);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItem(player.getLocation(), leftover.get(0));
            }
            player.sendMessage(dataManager.getConfigMessage("operation-success")
                    .replace("{amount}", String.valueOf(amount))
                    .replace("{box}", boxId));
        }
    }

    private void handleAdminClick(Player player, Inventory inv, ItemStack current, int slot) {
        String name = current.hasItemMeta() ? current.getItemMeta().getDisplayName() : "";
        if (name.contains("创建箱子")) {
            player.closeInventory();
            player.sendMessage(dataManager.getConfigMessage("probability-input-prompt").replace("{type}", "输入箱子ID："));
            pendingInput.put(player.getUniqueId(), new String[]{"create"});
        } else if (name.contains("编辑箱子")) {
            player.closeInventory();
            GUIHolder gui = (GUIHolder) inv.getHolder();
            player.openInventory(gui.createBoxSelectGUI());
        } else if (name.contains("发放钥匙")) {
            player.closeInventory();
            player.sendMessage(dataManager.getConfigMessage("probability-input-prompt").replace("{type}", "输入箱子ID和数量（空格分隔）："));
            pendingInput.put(player.getUniqueId(), new String[]{"key"});
        } else if (name.contains("重载配置")) {
            plugin.reloadConfig();
            dataManager.save();
            player.sendMessage(dataManager.getConfigMessage("reload-success"));
        }
    }

    private void handleEditClick(Player player, Inventory inv, ItemStack current, int slot) {
        String name = current.hasItemMeta() ? current.getItemMeta().getDisplayName() : "";
        if (name.equals(dataManager.getConfigMessage("edit-gui-save"))) {
            player.sendMessage(dataManager.getConfigMessage("operation-success"));
        } else if (name.equals(dataManager.getConfigMessage("edit-gui-exit"))) {
            player.closeInventory();
        }
        if (slot >= 9 && slot <= 17) {
            ItemStack item = inv.getItem(slot);
            if (item != null) {
                player.sendMessage(ChatColor.GRAY + "概率已调整（演示）");
            }
        }
    }

    private void handleSelectClick(Player player, Inventory inv, ItemStack current) {
        String boxId = current.hasItemMeta() ? current.getItemMeta().getDisplayName() : "";
        if (boxId.isEmpty()) return;
        DataManager.BoxData box = dataManager.getBox(boxId);
        if (box == null) {
            player.sendMessage(dataManager.getConfigMessage("chest-not-registered"));
            return;
        }
        boolean hasKey = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.PAPER && item.hasItemMeta()) {
                String dn = item.getItemMeta().getDisplayName();
                if (dn != null && dn.contains(boxId)) {
                    if (item.getAmount() >= 1) {
                        item.setAmount(item.getAmount() - 1);
                        hasKey = true;
                        break;
                    }
                }
            }
        }
        if (!hasKey) {
            player.sendMessage(dataManager.getConfigMessage("invalid-key-message"));
            return;
        }
        Map<String, Double> probs = box.getProbabilities();
        Map<String, List<ItemStack>> rewards = box.getRewards();
        if (probs.isEmpty()) {
            player.sendMessage(dataManager.getConfigMessage("lottery-result-message").replace("{item}", "无奖励"));
            return;
        }
        double rand = Math.random();
        double cumulative = 0.0;
        String selectedGrade = null;
        for (Map.Entry<String, Double> entry : probs.entrySet()) {
            cumulative += entry.getValue();
            if (rand <= cumulative) {
                selectedGrade = entry.getKey();
                break;
            }
        }
        if (selectedGrade == null) {
            selectedGrade = probs.keySet().iterator().next();
        }
        List<ItemStack> possibleRewards = rewards.getOrDefault(selectedGrade, Collections.emptyList());
        if (possibleRewards.isEmpty()) {
            player.sendMessage(dataManager.getConfigMessage("lottery-result-message").replace("{item}", "无奖励"));
            return;
        }
        ItemStack reward = possibleRewards.get(new Random().nextInt(possibleRewards.size()));
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(reward.clone());
        if (!leftover.isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), leftover.get(0));
        }
        GUIHolder gui = (GUIHolder) inv.getHolder();
        player.openInventory(gui.createResultGUI(Collections.singletonList(reward)));
    }
}