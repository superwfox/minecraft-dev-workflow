package com.tahai.sect;

import java.io.File;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private SectDataManager dataManager;
    private SectPapiExpansion papiExpansion;
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!new File(getDataFolder(), "messages.yml").exists()) {
            saveResource("messages.yml", false);
        }

        dataManager = new SectDataManager();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiExpansion = new SectPapiExpansion(dataManager);
        }

        PluginCommand sectCommand = getCommand("sect");
        SectCommand sectExecutor = new SectCommand(dataManager);
        sectCommand.setExecutor(sectExecutor);
        sectCommand.setTabCompleter(sectExecutor);

        chatListener = new ChatListener(dataManager);
        getServer().getPluginManager().registerEvents(new BlockSelectListener(), this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        getServer().getPluginManager().registerEvents(new WarDeathListener(dataManager), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(chatListener), this);

        new IncomeTask().runTaskTimer(this, 0L, 864000L);

        if (papiExpansion != null) {
            papiExpansion.register();
        }

        getLogger().info("Sect 插件已启用");
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            chatListener.removePendingInvite(player.getUniqueId());
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof SectGUI) {
                player.closeInventory();
            }
        }
        dataManager.save();
        dataManager.shutdown();
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Sect 插件已禁用");
    }

    public SectDataManager getDataManager() {
        return dataManager;
    }

    public SectPapiExpansion getPapiExpansion() {
        return papiExpansion;
    }
}