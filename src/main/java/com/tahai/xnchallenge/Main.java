package com.tahai.xnchallenge;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ConfigManager configManager;
    private ChallengeManager challengeManager;
    private ChallengeListener challengeListener;
    private XnCommand xnCommand;
    private WaveMonitorTask waveMonitorTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager();
        configManager.reload();

        challengeManager = new ChallengeManager();
        challengeListener = new ChallengeListener(configManager, challengeManager);
        xnCommand = new XnCommand();
        waveMonitorTask = new WaveMonitorTask(configManager);

        getCommand("xn").setExecutor(xnCommand);
        getCommand("xn").setTabCompleter(xnCommand);

        getServer().getPluginManager().registerEvents(challengeListener, this);
        getServer().getPluginManager().registerEvents(challengeManager, this);

        waveMonitorTask.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (challengeManager != null) {
            challengeManager.shutdown();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }

    public ChallengeListener getChallengeListener() {
        return challengeListener;
    }

    public XnCommand getXnCommand() {
        return xnCommand;
    }

    public WaveMonitorTask getWaveMonitorTask() {
        return waveMonitorTask;
    }
}