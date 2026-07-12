package com.tahai.prankplugin;

import com.tahai.prankplugin.PrankGui;
import com.tahai.prankplugin.PrankTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PrankGuiListener implements Listener, InventoryHolder {

    private static final String PRANK_MENU_TITLE = ChatColor.DARK_PURPLE + "选择恶搞类型";
    private static final int MENU_SIZE = 18;
    private static final Map<UUID, UUID> selectedTarget = new HashMap<>();
    private final Inventory prankMenu;
    private final PrankTaskManager taskManager;

    public PrankGuiListener() {
        this.taskManager = new PrankTaskManager();
        this.prankMenu = Bukkit.createInventory(this, MENU_SIZE, PRANK_MENU_TITLE);
        initializeMenu();
    }

    private void initializeMenu() {
        String[] pranks = {
                "§a反向操作", "§b随机传送", "§c声音惊吓", "§d假消息",
                "§e重命名玩家", "§6Bossbar标题", "§5重力反转", "§2随机天气",
                "§4随机爆炸", "§3假生物入侵", "§c停止目标玩家", "§7停止所有玩家"
        };
        for (int i = 0; i < pranks.length && i < MENU_SIZE; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(pranks[i]);
            item.setItemMeta(meta);
            prankMenu.setItem(i, item);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        Inventory clickedInv = event.getInventory();
        if (clickedInv.getHolder() instanceof PrankGui) {
            // Player selection GUI
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= clickedInv.getSize()) return;
            ItemStack current = clickedInv.getItem(slot);
            if (current == null || current.getType() != Material.PLAYER_HEAD) return;
            SkullMeta skullMeta = (SkullMeta) current.getItemMeta();
            if (skullMeta == null || skullMeta.getOwningPlayer() == null) return;
            UUID targetUUID = skullMeta.getOwningPlayer().getUniqueId();
            selectedTarget.put(clicker.getUniqueId(), targetUUID);
            clicker.closeInventory();
            clicker.openInventory(prankMenu);
        } else if (clickedInv.getHolder() == this) {
            // Prank type menu
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= MENU_SIZE) return;
            UUID targetUUID = selectedTarget.get(clicker.getUniqueId());
            if (targetUUID == null) {
                clicker.sendMessage(ChatColor.RED + "请先选择一个玩家！");
                return;
            }
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null || !target.isOnline()) {
                clicker.sendMessage(ChatColor.RED + "该玩家已离线！");
                return;
            }
            JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("PrankPlugin");
            switch (slot) {
                case 0: // 反向操作
                    runInvertView(target, plugin);
                    break;
                case 1: // 随机传送
                    runRandomTeleport(target, plugin);
                    break;
                case 2: // 声音惊吓
                    runSoundScare(target, plugin);
                    break;
                case 3: // 假消息
                    runFakeMessage(target, plugin);
                    break;
                case 4: // 重命名玩家
                    runRenamePlayer(target, plugin);
                    break;
                case 5: // Bossbar标题
                    runBossbarTitle(target, plugin);
                    break;
                case 6: // 重力反转
                    runGravityFlip(target, plugin);
                    break;
                case 7: // 随机天气
                    runRandomWeather(target, plugin);
                    break;
                case 8: // 随机爆炸
                    runRandomExplosion(target, plugin);
                    break;
                case 9: // 假生物入侵
                    runFakeMobInvasion(target, plugin);
                    break;
                case 10: // 停止目标玩家恶搞
                    taskManager.cancelPlayerTasks(target.getUniqueId());
                    target.sendMessage(ChatColor.GREEN + "你的恶搞已被停止！");
                    break;
                case 11: // 停止所有玩家恶搞
                    taskManager.cancelAllTasks();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "所有恶搞已被停止！");
                    break;
            }
        }
    }

    private void runInvertView(Player target, JavaPlugin plugin) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 200) { // 10 seconds
                    taskManager.cancelPlayerTasks(target.getUniqueId());
                    return;
                }
                target.setFlySpeed(-0.1f);
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        taskManager.addTask(target.getUniqueId(), "invert_view", this);
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("global_prefix", "&7[&6Prank&7] ") +
                plugin.getConfig().getString("messages.invert_view", "&c你的视角被反转了！")));
    }

    private void runRandomTeleport(Player target, JavaPlugin plugin) {
        World world = target.getWorld();
        Location loc = target.getLocation();
        Random rnd = new Random();
        double x = loc.getX() + (rnd.nextDouble() - 0.5) * 50;
        double z = loc.getZ() + (rnd.nextDouble() - 0.5) * 50;
        double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
        target.teleport(new Location(world, x, y, z));
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("global_prefix", "&7[&6Prank&7] ") +
                plugin.getConfig().getString("messages.random_teleport", "&5你被随机传送了！")));
    }

    private void runSoundScare(Player target, JavaPlugin plugin) {
        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_CREEPER_PRIMED, 1.0f, 0.0f);
        new BukkitRunnable() {
            @Override
            public void run() {
                target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_CREEPER_EXPLODE, 1.0f, 1.0f);
            }
        }.runTaskLater(plugin, 40L);
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("global_prefix", "&7[&6Prank&7] ") +
                plugin.getConfig().getString("messages.sound_scare", "&4吓到你了吗？")));
    }

    private void runFakeMessage(Player target, JavaPlugin plugin) {
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("global_prefix", "&7[&6Prank&7] ") +
                plugin.getConfig().getString("messages.fake_message", "&e这是一条假消息！")));
    }

    private void runRenamePlayer(Player target, JavaPlugin plugin) {
        String oldName = target.getName();
        String newName = "§k" + oldName + "§r";
        target.setDisplayName(newName);
        target.setPlayerListName(newName);
        new BukkitRunnable() {
            @Override
            public void run() {
                target.setDisplayName(oldName);
                target.setPlayerListName(oldName);
            }
        }.runTaskLater(plugin, 200L);
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("global_prefix", "&7[&6Prank&7] ") +
                plugin.getConfig().getString("messages.rename_player", "&a你的名字被改掉了！")));
    }

    private void runBossbarTitle(Player target, JavaPlugin plugin) {
        // Simple implementation using title
        target.sendTitle(ChatColor.DARK_RED + "恶搞标题", ChatColor.GRAY + "你的Bossbar被替换了", 10, 70, 20);
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("global_prefix", "&7[&6Prank&7] ") +
                plugin.getConfig().getString("messages.bossbar_title", "&6Bossbar被修改了！")));
    }

    private void runGravityFlip(Player target, JavaPlugin plugin) {
        target.setAllowFlight(true);
        target.setFlying(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                target.setFlying(false);
                target.setAllowFlight(false);
                target.setVelocity(new org.bukkit.util.Vector(0, -2, 0));
            }
        }.runTaskLater(plugin, 60L);
        taskManager.addTask(target.getUniqueId(), "gravity_flip", this);
        target.sendMessage(ChatColor.AQUA + "重力反转已启动！");
    }

    private void runRandomWeather(Player target, JavaPlugin plugin) {
        World world = target.getWorld();
        Random rnd = new Random();
        if (rnd.nextBoolean()) {
            world.setStorm(true);
            world.setThundering(true);
        } else {
            world.setStorm(false);
            world.setThundering(false);
        }
        target.sendMessage(ChatColor.YELLOW + "天气被随机改变了！");
    }

    private void runRandomExplosion(Player target, JavaPlugin plugin) {
        World world = target.getWorld();
        Location loc = target.getLocation();
        world.createExplosion(loc, 2.0f, false, false);
        target.sendMessage(ChatColor.RED + "炸弹爆炸！小心！");
    }

    private void runFakeMobInvasion(Player target, JavaPlugin plugin) {
        World world = target.getWorld();
        Location loc = target.getLocation();
        for (int i = 0; i < 5; i++) {
            world.spawnEntity(loc, EntityType.ZOMBIE);
        }
        target.sendMessage(ChatColor.DARK_GREEN + "僵尸入侵！");
        taskManager.addTask(target.getUniqueId(), "fake_mob_invasion", this);
    }

    @Override
    public Inventory getInventory() {
        return prankMenu;
    }
}