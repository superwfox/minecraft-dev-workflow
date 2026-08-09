package com.tahai.sect;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;
    private SectPlaceholderExpansion placeholderExpansion;
    private SectCreateListener createListener;
    private SectGuiListener guiListener;
    private SectCombatListener combatListener;
    private SectCommand sectCommand;
    private SectIncomeTask incomeTask;

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);
        dataManager.load();

        createListener = new SectCreateListener(dataManager);
        guiListener = new SectGuiListener(dataManager);
        combatListener = new SectCombatListener(dataManager);

        sectCommand = new SectCommand(dataManager, createListener);
        getCommand("sect").setExecutor(sectCommand);
        getCommand("sect").setTabCompleter(sectCommand);

        getServer().getPluginManager().registerEvents(createListener, this);
        getServer().getPluginManager().registerEvents(guiListener, this);
        getServer().getPluginManager().registerEvents(combatListener, this);

        placeholderExpansion = new SectPlaceholderExpansion(this);

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            getLogger().info("Vault 已挂钩");
        } else {
            getLogger().warning("未检测到 Vault，宗门俸禄功能将不可用");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion.register();
            getLogger().info("PlaceholderAPI 已挂钩");
        } else {
            getLogger().warning("未检测到 PlaceholderAPI，占位符功能将不可用");
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("WorldGuard 已挂钩");
        } else {
            getLogger().warning("未检测到 WorldGuard，领地功能将不可用");
        }

        incomeTask = new SectIncomeTask();
        incomeTask.runTaskTimer(this, 0L, 864000L);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public SectPlaceholderExpansion getPlaceholderExpansion() {
        return placeholderExpansion;
    }

    public SectCreateListener getCreateListener() {
        return createListener;
    }

    public SectGuiListener getGuiListener() {
        return guiListener;
    }

    public SectCombatListener getCombatListener() {
        return combatListener;
    }

    public SectCommand getSectCommand() {
        return sectCommand;
    }

    public SectIncomeTask getIncomeTask() {
        return incomeTask;
    }
}