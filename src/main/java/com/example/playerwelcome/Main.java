package com.example.playerwelcome;

import com.example.playerwelcome.config.ConfigManager;
import com.example.playerwelcome.listener.PlayerJoinListener;
import com.example.playerwelcome.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    private ConfigManager configManager;
    private MessageUtil messageUtil;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.saveDefaultConfig();

        // 初始化消息工具
        messageUtil = new MessageUtil();
        messageUtil.initialize();

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // 注册命令
        Objects.requireNonNull(getCommand("welcome")).setExecutor(this);

        getLogger().info("PlayerWelcome 插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("PlayerWelcome 插件已禁用。");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("welcome")) {
            if (args.length == 0) {
                // 显示用法
                messageUtil.sendMessage(sender, "&a用法: /welcome reload - 重载插件配置");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("playerwelcome.reload")) {
                    messageUtil.sendMessage(sender, "&c你没有权限执行此命令。");
                    return true;
                }

                // 重载配置
                configManager.reloadConfig();
                messageUtil.initialize(); // 重新初始化消息工具以应用新配置
                messageUtil.sendMessage(sender, "&a配置已重载！");
                return true;
            }
        }
        return false;
    }

    /**
     * 获取插件的配置管理器实例
     * @return 配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取插件的消息工具实例
     * @return 消息工具
     */
    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    /**
     * 获取插件实例的静态方法
     * @return 插件实例
     */
    public static Plugin getInstance() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerWelcome");
        if (plugin != null && plugin.isEnabled()) {
            return plugin;
        }
        throw new IllegalStateException("PlayerWelcome 插件未找到或未启用");
    }
}