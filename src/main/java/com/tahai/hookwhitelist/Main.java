package com.tahai.hookwhitelist;

import com.tahai.hookwhitelist.hook.HookPullTask;
import com.tahai.hookwhitelist.hook.PlayerHookListener;
import com.tahai.hookwhitelist.whitelist.HookWhitelistCommand;
import com.tahai.hookwhitelist.whitelist.WhitelistManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private WhitelistManager whitelistManager;
    private HookPullTask hookPullTask;
    private boolean whitelistLoaded;

    @Override
    public void onEnable() {
        whitelistManager = new WhitelistManager(this);
        try {
            whitelistManager.load();
            whitelistLoaded = true;
        } catch (RuntimeException ex) {
            whitelistLoaded = false;
            getLogger().warning("载入 whitelist.yml 失败，本次运行期间白名单可能为空；为避免用空表覆盖原有数据，关闭时将跳过写盘。");
            getLogger().warning(ex.toString());
        }

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
        getServer().getScheduler().cancelTasks(this);
        if (whitelistManager != null && whitelistLoaded) {
            whitelistManager.save();
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