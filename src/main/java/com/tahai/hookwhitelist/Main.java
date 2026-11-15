package com.tahai.hookwhitelist;

import com.tahai.hookwhitelist.hook.HookPullTask;
import com.tahai.hookwhitelist.hook.PlayerHookListener;
import com.tahai.hookwhitelist.whitelist.HookWhitelistCommand;
import com.tahai.hookwhitelist.whitelist.WhitelistManager;
import java.io.File;
import java.io.IOException;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private WhitelistManager whitelistManager;
    private HookPullTask hookPullTask;
    private boolean whitelistLoaded;

    @Override
    public void onEnable() {
        whitelistManager = new WhitelistManager(this);
        whitelistLoaded = loadWhitelistSafely();

        hookPullTask = new HookPullTask(this);

        HookWhitelistCommand command = new HookWhitelistCommand(this, whitelistManager);
        PluginCommand pluginCommand = getCommand("hookwl");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        } else {
            getLogger().warning("plugin.yml 中未声明命令 hookwl，/hookwl 与别名 /hwl 将不可用。");
        }

        PlayerHookListener listener = new PlayerHookListener(this, whitelistManager, hookPullTask);
        getServer().getPluginManager().registerEvents(listener, this);

        hookPullTask.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable() {
        endAllSessions();
        if (whitelistManager != null && whitelistLoaded) {
            whitelistManager.save();
        } else if (whitelistManager != null) {
            getLogger().warning("白名单未成功载入，已跳过写盘以保护原有 whitelist.yml。");
        }
        getServer().getScheduler().cancelTasks(this);
    }

    private boolean loadWhitelistSafely() {
        File file = new File(getDataFolder(), "whitelist.yml");
        if (file.exists()) {
            try {
                YamlConfiguration probe = new YamlConfiguration();
                probe.load(file);
            } catch (IOException ex) {
                getLogger().warning("读取 whitelist.yml 失败，本次运行期间白名单可能为空；为避免用空表覆盖原有数据，关闭时将跳过写盘。");
                getLogger().warning(ex.toString());
                return false;
            } catch (InvalidConfigurationException ex) {
                getLogger().warning("whitelist.yml 格式无效，本次运行期间白名单可能为空；为避免用空表覆盖原有数据，关闭时将跳过写盘。");
                getLogger().warning(ex.toString());
                return false;
            }
        }
        try {
            whitelistManager.load();
            return true;
        } catch (RuntimeException ex) {
            getLogger().warning("载入 whitelist.yml 失败，本次运行期间白名单可能为空；为避免用空表覆盖原有数据，关闭时将跳过写盘。");
            getLogger().warning(ex.toString());
            return false;
        }
    }

    private void endAllSessions() {
        if (hookPullTask == null) {
            return;
        }
        for (Player player : getServer().getOnlinePlayers()) {
            try {
                hookPullTask.endSession(player.getUniqueId());
            } catch (RuntimeException ignored) {
                // 单个玩家清理失败不应中断整体关闭流程
            }
        }
    }
}