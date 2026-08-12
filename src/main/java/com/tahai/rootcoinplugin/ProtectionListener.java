package com.tahai.rootcoinplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ProtectionListener implements Listener {

    private final Plugin plugin;

    public ProtectionListener() {
        this.plugin = Bukkit.getPluginManager().getPlugin("RootCoinPlugin");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isBound(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.AQUA + "请先使用 /bind 绑定QQ后再破坏方块。");
            return;
        }
        Location loc = event.getBlock().getLocation();
        if (isInLand(loc) && !isLandOwner(loc, player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.AQUA + "你没有权限在此领地内破坏方块。");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isBound(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.AQUA + "请先使用 /bind 绑定QQ后再放置方块。");
            return;
        }
        Location loc = event.getBlock().getLocation();
        if (isInLand(loc) && !isLandOwner(loc, player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.AQUA + "你没有权限在此领地内放置方块。");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.IRON_INGOT) return;
        if (!isBound(player)) {
            player.sendMessage(ChatColor.AQUA + "请先使用 /bind 绑定QQ购买领地。");
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) return;
        Location loc = block.getLocation();
        if (loc.getWorld().getEnvironment() != Environment.NORMAL) {
            player.sendMessage(ChatColor.AQUA + "只能在主世界购买领地。");
            return;
        }
        World world = loc.getWorld();
        Location spawn = world.getSpawnLocation();
        if (loc.distanceSquared(spawn) <= 320 * 320) {
            player.sendMessage(ChatColor.AQUA + "出生点20区块内不能购买领地。");
            return;
        }
        double balance = getBalance(player.getUniqueId());
        if (balance < 1000) {
            player.sendMessage(ChatColor.AQUA + "你的根号币不足1000。");
            return;
        }
        setBalance(player.getUniqueId(), balance - 1000);
        String landName = world.getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
        addLand(landName, player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "成功购买领地！");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getHolder() instanceof BlockState) {
            BlockState state = (BlockState) event.getInventory().getHolder();
            Location loc = state.getLocation();
            if (isInLand(loc) && !isLandOwner(loc, player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.AQUA + "你不能打开别人的领地箱子。");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (isInLand(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            if (isInLand(attacker.getLocation()) || isInLand(victim.getLocation())) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.AQUA + "你不能在领地内攻击其他玩家。");
            }
        }
    }

    private File getPlayersFile() {
        return new File(plugin.getDataFolder(), "players.yml");
    }

    private File getLandsFile() {
        return new File(plugin.getDataFolder(), "lands.yml");
    }

    private YamlConfiguration loadPlayersConfig() {
        return YamlConfiguration.loadConfiguration(getPlayersFile());
    }

    private YamlConfiguration loadLandsConfig() {
        return YamlConfiguration.loadConfiguration(getLandsFile());
    }

    private void savePlayersConfig(YamlConfiguration cfg) {
        try {
            cfg.save(getPlayersFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveLandsConfig(YamlConfiguration cfg) {
        try {
            cfg.save(getLandsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isBound(Player player) {
        return loadPlayersConfig().getString("players." + player.getUniqueId() + ".qq") != null;
    }

    private double getBalance(UUID uuid) {
        return loadPlayersConfig().getDouble("players." + uuid + ".balance", 0.0);
    }

    private void setBalance(UUID uuid, double balance) {
        YamlConfiguration cfg = loadPlayersConfig();
        cfg.set("players." + uuid + ".balance", balance);
        savePlayersConfig(cfg);
    }

    private String landNameAt(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private boolean isInLand(Location loc) {
        return loadLandsConfig().contains("lands." + landNameAt(loc));
    }

    private boolean isLandOwner(Location loc, UUID owner) {
        String ownerStr = loadLandsConfig().getString("lands." + landNameAt(loc) + ".owner");
        return ownerStr != null && ownerStr.equals(owner.toString());
    }

    private void addLand(String name, UUID owner) {
        YamlConfiguration cfg = loadLandsConfig();
        cfg.set("lands." + name + ".owner", owner.toString());
        saveLandsConfig(cfg);
    }
}