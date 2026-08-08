package com.tahai.minecartspeed;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinecartSpeedCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_SET = "minecartspeed.speedset";
    private static final String KEY_POS1 = "pos1";
    private static final String KEY_POS2 = "pos2";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.GRAY + "Usage: /minecartspeed <set <speed>|list|remove <index>|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                return handleSet(player, args);
            case "list":
                return handleList(player);
            case "remove":
                return handleRemove(player, args);
            case "reload":
                return handleReload(player);
            default:
                player.sendMessage(ChatColor.AQUA + "Unknown subcommand.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterSuggestions(new String[]{"set", "list", "remove", "reload"}, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove") && sender instanceof Player) {
            Player player = (Player) sender;
            RegionManager regionManager = new RegionManager();
            List<RegionData> regions = regionManager.getRegions(player.getWorld().getName());
            List<String> indices = new ArrayList<>();
            for (int i = 1; i <= regions.size(); i++) {
                indices.add(String.valueOf(i));
            }
            return filterSuggestions(indices, args[1]);
        }
        return Collections.emptyList();
    }

    private boolean handleSet(Player player, String[] args) {
        if (!player.hasPermission(PERMISSION_SET)) {
            player.sendMessage(ChatColor.AQUA + "You don't have permission to set speed regions.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.GRAY + "Usage: /minecartspeed set <speed>");
            return true;
        }

        double speed;
        try {
            speed = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.AQUA + "Speed must be a valid number.");
            return true;
        }

        ConfigManager config = ConfigManager.getInstance();
        if (speed < config.getSpeedMin() || speed > config.getSpeedMax()) {
            player.sendMessage(ChatColor.AQUA + "Speed must be between " + config.getSpeedMin() + " and " + config.getSpeedMax() + ".");
            return true;
        }

        RegionData selection = getPlayerSelection(player);
        if (selection == null) {
            player.sendMessage(ChatColor.AQUA + "You must select two points with a stick first.");
            return true;
        }

        selection.setWorld(player.getWorld().getName());
        selection.setSpeed(speed);

        RegionManager regionManager = new RegionManager();
        List<RegionData> existingRegions = regionManager.getRegions(selection.getWorld());
        RegionData existing = null;
        for (RegionData region : existingRegions) {
            if (sameRegion(region, selection)) {
                existing = region;
                break;
            }
        }

        if (existing != null) {
            regionManager.removeRegion(existing);
        }
        regionManager.addRegion(selection);
        regionManager.save();

        notifyPassengers(selection);
        player.sendMessage(ChatColor.YELLOW + "Speed region set to " + speed + ".");
        return true;
    }

    private boolean handleList(Player player) {
        RegionManager regionManager = new RegionManager();
        List<RegionData> regions = regionManager.getRegions(player.getWorld().getName());
        if (regions.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No speed regions in this world.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Speed regions in world " + player.getWorld().getName() + ":");
            for (int i = 0; i < regions.size(); i++) {
                RegionData region = regions.get(i);
                player.sendMessage(ChatColor.GRAY + "#" + (i + 1) + " (" +
                        region.getX1() + ", " + region.getY1() + ", " + region.getZ1() + ") to (" +
                        region.getX2() + ", " + region.getY2() + ", " + region.getZ2() + ") speed=" +
                        region.getSpeed());
            }
        }
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.GRAY + "Usage: /minecartspeed remove <index>");
            return true;
        }

        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.AQUA + "Invalid index.");
            return true;
        }

        RegionManager regionManager = new RegionManager();
        List<RegionData> regions = regionManager.getRegions(player.getWorld().getName());
        if (index < 1 || index > regions.size()) {
            player.sendMessage(ChatColor.AQUA + "Region index out of range.");
            return true;
        }

        regionManager.removeRegion(regions.get(index - 1));
        regionManager.save();
        player.sendMessage(ChatColor.YELLOW + "Region removed.");
        return true;
    }

    private boolean handleReload(Player player) {
        ConfigManager.getInstance().reload();
        player.sendMessage(ChatColor.YELLOW + "Configuration reloaded.");
        return true;
    }

    private RegionData getPlayerSelection(Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MinecartSpeed");
        if (plugin == null) return null;

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key1 = new NamespacedKey(plugin, KEY_POS1);
        NamespacedKey key2 = new NamespacedKey(plugin, KEY_POS2);

        if (!pdc.has(key1, PersistentDataType.STRING) || !pdc.has(key2, PersistentDataType.STRING)) {
            return null;
        }

        String[] pos1 = pdc.get(key1, PersistentDataType.STRING).split(",");
        String[] pos2 = pdc.get(key2, PersistentDataType.STRING).split(",");
        if (pos1.length != 3 || pos2.length != 3) return null;

        try {
            double x1 = Double.parseDouble(pos1[0]);
            double y1 = Double.parseDouble(pos1[1]);
            double z1 = Double.parseDouble(pos1[2]);
            double x2 = Double.parseDouble(pos2[0]);
            double y2 = Double.parseDouble(pos2[1]);
            double z2 = Double.parseDouble(pos2[2]);
            return new RegionData(player.getWorld().getName(), x1, y1, z1, x2, y2, z2, 0);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean sameRegion(RegionData a, RegionData b) {
        if (!a.getWorld().equals(b.getWorld())) return false;
        return (a.getX1() == b.getX1() && a.getY1() == b.getY1() && a.getZ1() == b.getZ1() &&
                a.getX2() == b.getX2() && a.getY2() == b.getY2() && a.getZ2() == b.getZ2()) ||
               (a.getX1() == b.getX2() && a.getY1() == b.getY2() && a.getZ1() == b.getZ2() &&
                a.getX2() == b.getX1() && a.getY2() == b.getY1() && a.getZ2() == b.getZ1());
    }

    private void notifyPassengers(RegionData region) {
        World world = Bukkit.getWorld(region.getWorld());
        if (world == null) return;

        boolean ignoreY = ConfigManager.getInstance().isIgnoreY();
        double minX = Math.min(region.getX1(), region.getX2());
        double maxX = Math.max(region.getX1(), region.getX2());
        double minY = Math.min(region.getY1(), region.getY2());
        double maxY = Math.max(region.getY1(), region.getY2());
        double minZ = Math.min(region.getZ1(), region.getZ2());
        double maxZ = Math.max(region.getZ1(), region.getZ2());

        String message = ConfigManager.getInstance().getActionbarChange()
                .replace("{speed}", String.valueOf(region.getSpeed()));

        for (Minecart minecart : world.getEntitiesByClass(Minecart.class)) {
            Location loc = minecart.getLocation();
            boolean inside = loc.getX() >= minX && loc.getX() <= maxX &&
                             loc.getZ() >= minZ && loc.getZ() <= maxZ;
            if (inside && (ignoreY || (loc.getY() >= minY && loc.getY() <= maxY))) {
                for (Entity passenger : minecart.getPassengers()) {
                    if (passenger instanceof Player) {
                        Player p = (Player) passenger;
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                    }
                }
            }
        }
    }

    private List<String> filterSuggestions(String[] suggestions, String token) {
        List<String> result = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(token.toLowerCase())) {
                result.add(suggestion);
            }
        }
        return result;
    }

    private List<String> filterSuggestions(List<String> suggestions, String token) {
        List<String> result = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(token.toLowerCase())) {
                result.add(suggestion);
            }
        }
        return result;
    }
}