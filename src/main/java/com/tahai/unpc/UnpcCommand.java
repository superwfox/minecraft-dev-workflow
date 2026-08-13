package com.tahai.unpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UnpcCommand implements CommandExecutor, TabCompleter {

    private final NpcManager npcManager;

    public UnpcCommand(NpcManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("unpc.cmd")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行此命令。");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Unpc");
        if (plugin == null) {
            sender.sendMessage(ChatColor.AQUA + "插件未加载。");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help" -> sendHelp(sender);
            case "create" -> handleCreate(sender, args, plugin);
            case "setdisplay" -> handleSetDisplay(sender, args);
            case "setskin" -> handleSetSkin(sender, args, plugin);
            case "delete" -> handleDelete(sender, args);
            case "setcommand" -> handleSetCommand(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "—— Unpc 帮助 ——");
        sender.sendMessage(ChatColor.YELLOW + "/unpc help" + ChatColor.WHITE + " 显示帮助");
        sender.sendMessage(ChatColor.YELLOW + "/unpc create <显示文字> [皮肤玩家名]" + ChatColor.WHITE + " 创建NPC");
        sender.sendMessage(ChatColor.YELLOW + "/unpc setdisplay <id> <显示文字>" + ChatColor.WHITE + " 修改显示文字");
        sender.sendMessage(ChatColor.YELLOW + "/unpc setskin <id> <正版玩家名>" + ChatColor.WHITE + " 修改皮肤");
        sender.sendMessage(ChatColor.YELLOW + "/unpc delete <id>" + ChatColor.WHITE + " 删除NPC");
        sender.sendMessage(ChatColor.YELLOW + "/unpc setcommand <id> <player|console> <命令>" + ChatColor.WHITE + " 设置右键命令");
        sender.sendMessage(ChatColor.YELLOW + "/unpc reload" + ChatColor.WHITE + " 重载配置");
    }

    private void handleCreate(CommandSender sender, String[] args, Plugin plugin) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.AQUA + "该命令只能由玩家执行。");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.AQUA + "用法: /unpc create <显示文字> [皮肤玩家名]");
            return;
        }
        String displayText = args[1];
        String skinName = args.length >= 3 ? args[2] : null;
        Location loc = player.getLocation();
        NpcData npc = npcManager.create(loc, displayText, skinName, null, null, null, "player");
        sender.sendMessage(ChatColor.YELLOW + "NPC 已创建，ID: " + npc.getId());
        if (skinName != null) {
            applySkinAsync(npc.getId(), skinName, sender, plugin);
        }
    }

    private void handleSetDisplay(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.AQUA + "用法: /unpc setdisplay <id> <显示文字>");
            return;
        }
        int id = parseInt(sender, args[1]);
        if (id == -1) return;
        String text = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        npcManager.setDisplay(id, text);
        sender.sendMessage(ChatColor.YELLOW + "已修改 NPC " + id + " 的显示文字。");
    }

    private void handleSetSkin(CommandSender sender, String[] args, Plugin plugin) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.AQUA + "用法: /unpc setskin <id> <正版玩家名>");
            return;
        }
        int id = parseInt(sender, args[1]);
        if (id == -1) return;
        String playerName = args[2];
        applySkinAsync(id, playerName, sender, plugin);
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.AQUA + "用法: /unpc delete <id>");
            return;
        }
        int id = parseInt(sender, args[1]);
        if (id == -1) return;
        npcManager.delete(id);
        sender.sendMessage(ChatColor.YELLOW + "已删除 NPC " + id + "。");
    }

    private void handleSetCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.AQUA + "用法: /unpc setcommand <id> <player|console> <命令>");
            return;
        }
        int id = parseInt(sender, args[1]);
        if (id == -1) return;
        String executor = args[2].toLowerCase();
        if (!executor.equals("player") && !executor.equals("console")) {
            sender.sendMessage(ChatColor.AQUA + "执行身份必须是 player 或 console。");
            return;
        }
        String cmd = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
        npcManager.setCommand(id, cmd, executor);
        sender.sendMessage(ChatColor.YELLOW + "已设置 NPC " + id + " 的右键命令。");
    }

    private void handleReload(CommandSender sender) {
        npcManager.reload();
        sender.sendMessage(ChatColor.YELLOW + "NPC 配置已重载。");
    }

    private int parseInt(CommandSender sender, String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.AQUA + "无效的ID: " + text);
            return -1;
        }
    }

    private void applySkinAsync(int id, String playerName, CommandSender sender, Plugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String[] skin = null;
            try {
                skin = fetchSkin(playerName);
            } catch (Exception ignored) {
            }
            String[] result = skin;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (result == null) {
                    sender.sendMessage(ChatColor.AQUA + "无法获取玩家 " + playerName + " 的皮肤。");
                    return;
                }
                npcManager.setSkin(id, playerName, result[0], result[1]);
                sender.sendMessage(ChatColor.YELLOW + "已更新 NPC " + id + " 的皮肤。");
            });
        });
    }

    private String[] fetchSkin(String playerName) throws Exception {
        URL uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
        String uuidBody;
        try (InputStream in = uuidUrl.openStream()) {
            uuidBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        JsonObject uuidObj = JsonParser.parseString(uuidBody).getAsJsonObject();
        if (!uuidObj.has("id")) return null;

        String uuid = uuidObj.get("id").getAsString();
        URL profileUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        String profileBody;
        try (InputStream in = profileUrl.openStream()) {
            profileBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        JsonObject profile = JsonParser.parseString(profileBody).getAsJsonObject();
        JsonArray properties = profile.getAsJsonArray("properties");
        if (properties == null) return null;
        for (int i = 0; i < properties.size(); i++) {
            JsonObject prop = properties.get(i).getAsJsonObject();
            if ("textures".equals(prop.get("name").getAsString())) {
                return new String[]{prop.get("value").getAsString(), prop.get("signature").getAsString()};
            }
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("unpc.cmd")) return Collections.emptyList();
        if (args.length == 1) {
            return filterStart(args[0], List.of("help", "create", "setdisplay", "setskin", "delete", "setcommand", "reload"));
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("setdisplay") || sub.equals("setskin") || sub.equals("delete") || sub.equals("setcommand")) {
                return filterStart(args[1], getNpcIds());
            }
            if (sub.equals("create")) {
                return filterStart(args[1], getOnlinePlayerNames());
            }
        }
        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("setskin") || sub.equals("create")) {
                return filterStart(args[2], getOnlinePlayerNames());
            }
            if (sub.equals("setcommand")) {
                return filterStart(args[2], List.of("player", "console"));
            }
        }
        return Collections.emptyList();
    }

    private List<String> getNpcIds() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Unpc");
        if (plugin == null) return Collections.emptyList();
        List<String> ids = new ArrayList<>();
        try {
            List<Map<?, ?>> npcs = plugin.getConfig().getMapList("npcs");
            for (Map<?, ?> npc : npcs) {
                Object id = npc.get("id");
                if (id != null) ids.add(String.valueOf(id));
            }
        } catch (Exception ignored) {
        }
        return ids;
    }

    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
        return names;
    }

    private List<String> filterStart(String prefix, List<String> candidates) {
        List<String> result = new ArrayList<>();
        for (String c : candidates) {
            if (c.toLowerCase().startsWith(prefix.toLowerCase())) result.add(c);
        }
        return result;
    }
}