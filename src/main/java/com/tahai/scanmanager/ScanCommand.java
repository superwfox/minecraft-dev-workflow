import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ScanCommand implements CommandExecutor, TabCompleter {
    private DataManager dataManager;

    private DataManager getDataManager() {
        if (dataManager == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("ScanManager");
            dataManager = new DataManager(plugin);
        }
        return dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("scan.admin")) {
            sender.sendMessage(ChatColor.AQUA + "你没有权限执行 /scan 命令");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.GRAY + "用法: /scan <目标> <only|overall>");
            return true;
        }
        String mode = args[1].toLowerCase();
        if (!mode.equals("only") && !mode.equals("overall")) {
            sender.sendMessage(ChatColor.GRAY + "模式必须为 only 或 overall");
            return true;
        }
        DataManager dm = getDataManager();
        String target = args[0];
        List<String> targetIPs = new ArrayList<>();
        if (isIP(target)) {
            targetIPs.add(target);
        } else {
            UUID uuid = getPlayerUUID(target);
            if (uuid == null) {
                sender.sendMessage(ChatColor.AQUA + "无法解析玩家: " + target);
                return true;
            }
            List<String> playerIPs = dm.getIPsByUUID(uuid);
            if (playerIPs.isEmpty()) {
                sender.sendMessage(ChatColor.AQUA + "该玩家没有历史 IP 记录");
                return true;
            }
            if (mode.equals("only")) {
                targetIPs.add(playerIPs.get(playerIPs.size() - 1));
            } else {
                targetIPs.addAll(playerIPs);
            }
        }
        for (String ip : targetIPs) {
            List<UUID> uuids = dm.getUUIDsByIP(ip);
            List<String> names = new ArrayList<>();
            for (UUID id : uuids) {
                String name = Bukkit.getOfflinePlayer(id).getName();
                names.add(name == null ? id.toString() : name);
            }
            String msg = ChatColor.YELLOW + "IP " + ip + ChatColor.GRAY + " 关联玩家: " + String.join(", ", names);
            sender.sendMessage(msg);
        }
        return true;
    }

    private boolean isIP(String ip) {
        return ip.matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    private UUID getPlayerUUID(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            return player.getUniqueId();
        }
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("scan.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        } else if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String mode : Arrays.asList("only", "overall")) {
                if (mode.startsWith(prefix)) {
                    completions.add(mode);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}