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

    @Override
    public void onEnable() {
        whitelistManager = new WhitelistManager(this);
        whitelistManager.load();

        hookPullTask = new HookPullTask(this);

        HookWhitelistCommand command = new HookWhitelistCommand(this, whitelistManager);
        PluginCommand pluginCommand = getCommand("hookwl");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        PlayerHookListener listener = new PlayerHookListener(this, whitelistManager, hookPullTask);
        getServer().getPluginManager().registerEvents(listener, this);

        hookPullTask.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable() {
        endAllSessions();
        getServer().getScheduler().cancelTasks(this);
        if (whitelistManager != null) {
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