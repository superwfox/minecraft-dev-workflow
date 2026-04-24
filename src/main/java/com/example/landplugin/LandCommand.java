package com.example.landplugin;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class LandCommand implements CommandExecutor {

    private final LandManager landManager;
    private final InventoryGUI inventoryGUI;

    public LandCommand(LandManager landManager, InventoryGUI inventoryGUI) {
        this.landManager = landManager;
        this.inventoryGUI = inventoryGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§6===== 领地系统帮助 =====");
            player.sendMessage("§e/land create §7- 创建领地");
            player.sendMessage("§e/land delete §7- 删除领地");
            player.sendMessage("§e/land info §7- 查看领地信息");
            player.sendMessage("§e/land list §7- 查看你的领地列表");
            player.sendMessage("§e/land trust <玩家名> §7- 信任玩家");
            player.sendMessage("§e/land untrust <玩家名> §7- 取消信任");
            player.sendMessage("§e/land gui §7- 打开领地管理GUI");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player);
            case "delete":
                return handleDelete(player);
            case "info":
                return handleInfo(player);
            case "list":
                return handleList(player);
            case "trust":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /land trust <玩家名>");
                    return true;
                }
                return handleTrust(player, args[1]);
            case "untrust":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /land untrust <玩家名>");
                    return true;
                }
                return handleUntrust(player, args[1]);
            case "gui":
                return handleGui(player);
            default:
                player.sendMessage("§c未知子命令！请输入 /land 查看帮助");
                return true;
        }
    }

    private boolean handleCreate(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        LandData existing = landManager.getLand(chunk);
        if (existing != null) {
            player.sendMessage("§c此区块已被占地！");
            return true;
        }

        boolean success = landManager.createLand(chunk, player);
        if (success) {
            player.sendMessage("§a成功创建领地！");
        } else {
            player.sendMessage("§c创建领地失败！");
        }
        return true;
    }

    private boolean handleDelete(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        LandData land = landManager.getLand(chunk);
        if (land == null) {
            player.sendMessage("§c此区块还没有领地！");
            return true;
        }

        if (!land.isOwner(player.getUniqueId())) {
            player.sendMessage("§c你不是这个领地的所有者！");
            return true;
        }

        boolean success = landManager.deleteLand(chunk);
        if (success) {
            player.sendMessage("§a成功删除领地！");
        } else {
            player.sendMessage("§c删除领地失败！");
        }
        return true;
    }

    private boolean handleInfo(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        LandData land = landManager.getLand(chunk);
        if (land == null) {
            player.sendMessage("§c此区块还没有领地！");
            return true;
        }

        player.sendMessage("§6===== 领地信息 =====");
        player.sendMessage("§e世界: §7" + land.getWorldName());
        player.sendMessage("§e坐标: §7X:" + land.getChunkX() + " Z:" + land.getChunkZ());
        String ownerName = Bukkit.getOfflinePlayer(land.getOwnerUUID()).getName();
        player.sendMessage("§e所有者: §7" + (ownerName != null ? ownerName : land.getOwnerUUID().toString()));
        StringBuilder trustedList = new StringBuilder();
        for (UUID uuid : land.getTrustedMembers()) {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            trustedList.append(name != null ? name : uuid.toString()).append(", ");
        }
        if (trustedList.length() > 0) {
            trustedList.setLength(trustedList.length() - 2);
            player.sendMessage("§e信任玩家: §7" + trustedList.toString());
        } else {
            player.sendMessage("§e信任玩家: §7无");
        }
        return true;
    }

    private boolean handleList(Player player) {
        inventoryGUI.openLandListGUI(player);
        return true;
    }

    private boolean handleTrust(Player player, String targetName) {
        Chunk chunk = player.getLocation().getChunk();
        LandData land = landManager.getLand(chunk);
        if (land == null) {
            player.sendMessage("§c此区块还没有领地！");
            return true;
        }

        if (!land.isOwner(player.getUniqueId())) {
            player.sendMessage("§c你不是这个领地的所有者！");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§c玩家不在线或不存在！");
            return true;
        }

        if (land.isTrusted(target.getUniqueId())) {
            player.sendMessage("§c该玩家已经被信任！");
            return true;
        }

        boolean success = landManager.addTrusted(chunk, target.getUniqueId());
        if (success) {
            player.sendMessage("§a成功信任玩家 " + target.getName());
            target.sendMessage("§a你已被 " + player.getName() + " 信任到他的领地！");
        } else {
            player.sendMessage("§c信任操作失败！");
        }
        return true;
    }

    private boolean handleUntrust(Player player, String targetName) {
        Chunk chunk = player.getLocation().getChunk();
        LandData land = landManager.getLand(chunk);
        if (land == null) {
            player.sendMessage("§c此区块还没有领地！");
            return true;
        }

        if (!land.isOwner(player.getUniqueId())) {
            player.sendMessage("§c你不是这个领地的所有者！");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§c玩家不在线或不存在！");
            return true;
        }

        if (!land.isTrusted(target.getUniqueId())) {
            player.sendMessage("§c该玩家没有被信任！");
            return true;
        }

        boolean success = landManager.removeTrusted(chunk, target.getUniqueId());
        if (success) {
            player.sendMessage("§a已取消信任玩家 " + target.getName());
            target.sendMessage("§c你已被 " + player.getName() + " 取消信任！");
        } else {
            player.sendMessage("§c取消信任操作失败！");
        }
        return true;
    }

    private boolean handleGui(Player player) {
        inventoryGUI.openLandListGUI(player);
        return true;
    }
}