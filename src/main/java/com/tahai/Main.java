package com.tahai.laowu;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件唯一主类，所有配置读取、配对任务全部内嵌，仅使用截图5个文件
 * Spigot1.21.1 Java17
 */
public class Main extends JavaPlugin implements CommandExecutor {
    // 缓存所有运行中的猫咪配对任务
    private final List<CatPairTask> activeTasks = new ArrayList<>();

    // 音效封装记录类
    public record SoundCfg(Sound sound, float volume, float pitch) {}

    // 单组两只猫咪配对定时任务 内嵌静态内部类，不新建文件
    private static class CatPairTask extends BukkitRunnable {
        private final Main plugin;
        private final Cat catA;
        private final Cat catB;
        private int tickCount = 0;
        private float headOffset = 0F;
        private final SoundCfg pairSound;
        private final SoundCfg adsorbSound;
        private final SoundCfg fightStart;
        private final SoundCfg fightLoop;
        private final SoundCfg fightEnd;

        public CatPairTask(Main plugin, Cat a, Cat b) {
            this.plugin = plugin;
            this.catA = a;
            this.catB = b;
            // 预读取全部音效
            this.pairSound = plugin.getSound("pair");
            this.adsorbSound = plugin.getSound("adsorb_loop");
            this.fightStart = plugin.getSound("fight_start");
            this.fightLoop = plugin.getSound("fight_loop");
            this.fightEnd = plugin.getSound("fight_end");
            // 配对成功音效一次性播放
            playSound(catA.getLocation(), pairSound);
        }

        @Override
        public void run() {
            // 校验猫咪有效性，失效直接终止任务
            if (!isCatValid(catA) || !isCatValid(catB)) {
                plugin.removeTask(this);
                return;
            }
            tickCount++;
            Location locA = catA.getLocation();
            Location locB = catB.getLocation();
            double dist = locA.distance(locB);

            // 达到最大互动时长结束
            int maxTick = plugin.getConfig().getInt("fight-tick-duration", 400);
            if (tickCount >= maxTick) {
                playSound(locA, fightEnd);
                plugin.removeTask(this);
                return;
            }

            // 磁铁吸附逻辑
            double adsorbDist = plugin.getConfig().getDouble("adsorb-distance", 3.0);
            double step = plugin.getConfig().getDouble("move-step", 0.15);
            if (dist <= adsorbDist) {
                Vector toB = locB.subtract(locA).toVector().normalize().multiply(step);
                catA.setVelocity(toB);
                Vector toA = locA.subtract(locB).toVector().normalize().multiply(step);
                catB.setVelocity(toA);
                // 循环吸附音效
                if (tickCount % 20 == 0) playSound(locA, adsorbSound);
            }

            // 打闹音效与粒子
            if (tickCount == 10) playSound(locA, fightStart);
            if (tickCount % 15 == 0) {
                playSound(locA, fightLoop);
                int particleNum = plugin.getConfig().getInt("particle-count", 8);
                locA.getWorld().spawnParticle(Particle.HEART, locA, particleNum, 0.3,0.3,0.3,0.02);
                locB.getWorld().spawnParticle(Particle.HEART, locB, particleNum, 0.3,0.3,0.3,0.02);
            }

            // 回血逻辑
            boolean healEnable = plugin.getConfig().getBoolean("enable-heal", true);
            double healVal = plugin.getConfig().getDouble("heal-amount", 0.5);
            if (healEnable && tickCount % 30 == 0) {
                catA.setHealth(Math.min(catA.getHealth() + healVal, catA.getMaxHealth()));
                catB.setHealth(Math.min(catB.getHealth() + healVal, catB.getMaxHealth()));
            }

            // 弓背动画
            boolean archEnable = plugin.getConfig().getBoolean("enable-arch-back", true);
            if (archEnable) {
                catA.setSitting(false);
                catB.setSitting(false);
                catA.setAggressive(true);
                catB.setAggressive(true);
            }

            // 歪头摆动 setHeadYaw核心逻辑
            boolean swingEnable = plugin.getConfig().getBoolean("enable-head-swing", true);
            int swingTick = plugin.getConfig().getInt("head-swing-tick", 5);
            double maxAngle = plugin.getConfig().getDouble("head-max-angle", 45.0);
            if (swingEnable && tickCount % swingTick == 0) {
                headOffset += 1.5F;
                float swing = (float) Math.sin(Math.toRadians(headOffset)) * (float) maxAngle;
                catA.setHeadYaw(locA.getYaw() + swing);
                catB.setHeadYaw(locB.getYaw() - swing);
            }
        }

        // 校验猫咪存活、未卸载
        private boolean isCatValid(Cat cat) {
            return cat.isValid() && !cat.isDead() && cat.getWorld() != null;
        }

        // 播放音效工具
        private void playSound(Location loc, SoundCfg cfg) {
            loc.getWorld().playSound(loc, cfg.sound(), cfg.volume(), cfg.pitch());
        }

        // 插件关闭重置猫咪状态
        public void resetCat() {
            if (isCatValid(catA)) {
                catA.setAggressive(false);
                catA.setVelocity(Vector.ZERO);
            }
            if (isCatValid(catB)) {
                catB.setAggressive(false);
                catB.setVelocity(Vector.ZERO);
            }
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // 注册指令
        getCommand("laowu").setExecutor(this);
        getLogger().info("§a老吴插件加载完成，/laowu trigger 触发猫咪互动");
    }

    @Override
    public void onDisable() {
        // 终止全部任务、重置猫咪状态
        for (CatPairTask task : activeTasks) {
            task.cancel();
            task.resetCat();
        }
        activeTasks.clear();
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("§c老吴插件卸载，所有猫咪互动终止");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("trigger")) {
            sender.sendMessage("§e用法：/laowu trigger");
            return true;
        }
        if (!(sender instanceof Entity center)) {
            sender.sendMessage("§c仅玩家可执行该指令");
            return true;
        }

        // 扫描范围内所有有效猫咪
        int radius = getConfig().getInt("scan-radius", 20);
        List<Cat> catList = new ArrayList<>();
        for (Entity e : center.getWorld().getNearbyEntities(center.getLocation(), radius, radius, radius)) {
            if (e instanceof Cat cat && cat.isValid() && !cat.isDead()) {
                catList.add(cat);
            }
        }

        if (catList.size() < 2) {
            sender.sendMessage("§c有效猫咪不足2只，当前："+catList.size());
            return true;
        }

        // 清空旧任务，新建两两配对
        clearAllTasks();
        for (int i = 0; i < catList.size()-1; i += 2) {
            Cat a = catList.get(i);
            Cat b = catList.get(i+1);
            CatPairTask task = new CatPairTask(this, a, b);
            task.runTaskTimer(this, 0, 1);
            activeTasks.add(task);
        }
        sender.sendMessage("§a成功触发配对，猫咪总数："+catList.size());
        return true;
    }

    // 读取音效配置工具方法
    public SoundCfg getSound(String path) {
        ConfigurationSection sec = getConfig().getConfigurationSection("sound."+path);
        if (sec == null) return new SoundCfg(Sound.ENTITY_CAT_PURR,1f,1f);
        Sound s;
        try {
            s = Sound.valueOf(sec.getString("name","ENTITY_CAT_PURR"));
        }catch (IllegalArgumentException e){
            s = Sound.ENTITY_CAT_PURR;
        }
        float vol = (float) sec.getDouble("volume",1);
        float pit = (float) sec.getDouble("pitch",1);
        return new SoundCfg(s,vol,pit);
    }

    // 移除单个失效任务
    public void removeTask(CatPairTask task) {
        task.cancel();
        task.resetCat();
        activeTasks.remove(task);
    }

    // 清空全部配对任务
    public void clearAllTasks() {
        for (CatPairTask t : activeTasks) {
            t.cancel();
            t.resetCat();
        }
        activeTasks.clear();
    }
}
