package com.tahai.lottery;

import java.util.*;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class GUIHolder implements Listener, InventoryHolder {

    private final Inventory inventory;
    private final GUIType type;
    private final Location chestLoc;
    private final List<ItemStack> rewards;
    private final Consumer<String> callback;

    private enum GUIType {
        ADMIN, EDIT, BOX_SELECT, RESULT, PROBABILITY_INPUT
    }

    private GUIHolder(GUIType type, Location chestLoc, List<ItemStack> rewards, Consumer<String> callback) {
        this.type = type;
        this.chestLoc = chestLoc;
        this.rewards = rewards;
        this.callback = callback;
        Plugin plugin = (Plugin) Bukkit.getPluginManager().getPlugin("Lottery");
        String title;
        int size;
        switch (type) {
            case ADMIN:
                title = plugin.getConfig().getString("admin-gui-title", "管理GUI");
                size = 54;
                break;
            case EDIT:
                title = plugin.getConfig().getString("edit-gui-title", "编辑箱子");
                size = 27;
                break;
            case BOX_SELECT:
                title = plugin.getConfig().getString("box-select-gui-title", "选择抽奖箱");
                size = 54;
                break;
            case RESULT:
                title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lottery-result-message", "开箱结果"));
                size = 54;
                break;
            case PROBABILITY_INPUT:
                title = ChatColor.YELLOW + "输入概率";
                size = 9;
                break;
            default:
                title = "";
                size = 9;
        }
        this.inventory = Bukkit.createInventory(this, size, title);
        if (type == GUIType.PROBABILITY_INPUT) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "点击输入概率");
            paper.setItemMeta(meta);
            inventory.setItem(4, paper);
        }
    }

    public static Inventory createAdminGUI() {
        return new GUIHolder(GUIType.ADMIN, null, null, null).getInventory();
    }

    public static Inventory createEditGUI(Location chestLoc) {
        return new GUIHolder(GUIType.EDIT, chestLoc, null, null).getInventory();
    }

    public static Inventory createBoxSelectGUI() {
        return new GUIHolder(GUIType.BOX_SELECT, null, null, null).getInventory();
    }

    public static Inventory createResultGUI(List<ItemStack> rewards) {
        return new GUIHolder(GUIType.RESULT, null, rewards, null).getInventory();
    }

    public static void openProbabilityInput(Player player, Consumer<Double> callback) {
        GUIHolder holder = new GUIHolder(GUIType.PROBABILITY_INPUT, null, null, s -> {
            try {
                double prob = Double.parseDouble(s);
                callback.accept(prob);
            } catch (NumberFormatException e) {
                Plugin plugin = (Plugin) Bukkit.getPluginManager().getPlugin("Lottery");
                String raw = plugin.getConfig().getString("invalid-key-message", "无效的数字，请重新输入");
                String msg = ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', raw);
                player.sendMessage(msg);
            }
        });
        player.openInventory(holder.getInventory());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof GUIHolder holder) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getInventory().getSize()) return;

            if (holder.type == GUIType.PROBABILITY_INPUT && event.getRawSlot() == 4) {
                event.getWhoClicked().closeInventory();
                Player player = (Player) event.getWhoClicked();
                Plugin plugin = (Plugin) Bukkit.getPluginManager().getPlugin("Lottery");
                String rawPrompt = plugin.getConfig().getString("probability-input-prompt", "请在聊天框输入概率（0-1之间的小数）：");
                String prompt = ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', rawPrompt);
                ConversationFactory factory = new ConversationFactory(plugin)
                        .withFirstPrompt(new ProbabilityPrompt(holder.callback, prompt))
                        .withLocalEcho(false)
                        .withTimeout(30);
                Conversation conv = factory.buildConversation(player);
                conv.begin();
            }
        }
    }

    private static class ProbabilityPrompt extends StringPrompt {
        private final Consumer<String> callback;
        private final String prompt;

        ProbabilityPrompt(Consumer<String> callback, String prompt) {
            this.callback = callback;
            this.prompt = prompt;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return prompt;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (callback != null) {
                callback.accept(input);
            }
            return Prompt.END_OF_CONVERSATION;
        }
    }
}