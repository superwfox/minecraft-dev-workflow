package com.emeraldelevator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJumpEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Main extends JavaPlugin implements Listener {
    private Map<Player, Long> cooldownMap;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        cooldownMap = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("绿宝石电梯加载完成!");
    }

    //跳跃向上传送
    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("elevator.use")) return;
        Block standBlock = player.getLocation().subtract(0,1,0).getBlock();
        Material elevatorMat = Material.valueOf(config.getString("elevator-block", "EMERALD_BLOCK"));
        if(standBlock.getType() != elevatorMat) return;

        long cooldown = config.getLong("cooldown", 2000);
        if(cooldownMap.containsKey(player) && System.currentTimeMillis() - cooldownMap.get(player) < cooldown){
            player.sendMessage(config.getString("msg-cooldown", "&c冷却中，请稍后"));
            return;
        }

        Location start = standBlock.getLocation();
        Block targetBlock = findUp(start, elevatorMat);
        if(targetBlock == null){
            player.sendMessage(config.getString("msg-no-found", "&c没有找到对应电梯"));
            return;
        }

        Location targetLoc = targetBlock.getLocation().add(0,1,0);
        Block b1 = targetLoc.add(0,1,0).getBlock();
        Block b2 = targetLoc.add(0,1,0).getBlock();
        if(b1.getType().isSolid() || b2.getType().isSolid()){
            player.sendMessage("&c目标位置不安全！");
            return;
        }

        teleportPlayer(player, targetLoc);
        cooldownMap.put(player, System.currentTimeMillis());
        player.sendMessage(config.getString("msg-success", "&a传送成功"));
    }

    //蹲下向下传送
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if(!event.isSneaking()) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("elevator.use")) return;
        Block standBlock = player.getLocation().subtract(0,1,0).getBlock();
        Material elevatorMat = Material.valueOf(config.getString("elevator-block", "EMERALD_BLOCK"));
        if(standBlock.getType() != elevatorMat) return;

        long cooldown = config.getLong("cooldown", 2000);
        if(cooldownMap.containsKey(player) && System.currentTimeMillis() - cooldownMap.get(player) < cooldown){
            player.sendMessage(config.getString("msg-cooldown", "&c冷却中，请稍后"));
            return;
        }

        Location start = standBlock.getLocation();
        Block targetBlock = findDown(start, elevatorMat);
        if(targetBlock == null){
            player.sendMessage(config.getString("msg-no-found", "&c没有找到对应电梯"));
            return;
        }

        Location targetLoc = targetBlock.getLocation().add(0,1,0);
        Block b1 = targetLoc.add(0,1,0).getBlock();
        Block b2 = targetLoc.add(0,1,0).getBlock();
        if(b1.getType().isSolid() || b2.getType().isSolid()){
            player.sendMessage("&c目标位置不安全！");
            return;
        }

        teleportPlayer(player, targetLoc);
        cooldownMap.put(player, System.currentTimeMillis());
        player.sendMessage(config.getString("msg-success", "&a传送成功"));
    }

    //向上搜索同XZ Y轴
    private Block findUp(Location start, Material mat){
        int maxSearch = config.getInt("max-search", 128);
        for(int y = start.getBlockY()+1; y <= start.getBlockY()+maxSearch; y++){
            Block b = start.getWorld().getBlockAt(start.getBlockX(), y, start.getBlockZ());
            if(b.getType() == mat) return b;
        }
        return null;
    }

    //向下搜索同XZ Y轴
    private Block findDown(Location start, Material mat){
        int maxSearch = config.getInt("max-search", 128);
        for(int y = start.getBlockY()-1; y >= start.getBlockY()-maxSearch; y--){
            Block b = start.getWorld().getBlockAt(start.getBlockX(), y, start.getBlockZ());
            if(b.getType() == mat) return b;
        }
        return null;
    }

    //传送+粒子+音效
    private void teleportPlayer(Player p, Location target){
        if(config.getBoolean("enable-particle", true)){
            p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 30);
            p.getWorld().spawnParticle(Particle.PORTAL, target, 30);
        }
        if(config.getBoolean("enable-sound", true)){
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_PEARL_TELEPORT, 1,1);
            p.getWorld().playSound(target, Sound.ENTITY_ENDER_PEARL_TELEPORT, 1,1);
        }
        p.teleport(target);
    }
}
