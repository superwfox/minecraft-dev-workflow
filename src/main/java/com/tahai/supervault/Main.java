package com.tahai.supervault;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private PlayerVaultManager vaultManager;

    @Override
    public void onEnable() {
        vaultManager = new PlayerVaultManager(this);

        PluginCommand superChestCommand = getCommand("superchest");
        if (superChestCommand == null) {
            getLogger().severe("Command 'superchest' not found in plugin.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        SuperChestCommand executor = new SuperChestCommand();
        superChestCommand.setExecutor(executor);
        superChestCommand.setTabCompleter(executor);

        getServer().getPluginManager().registerEvents(new OpenVaultListener(vaultManager), this);
        getServer().getPluginManager().registerEvents(new VaultGuiListener(vaultManager), this);
    }

    @Override
    public void onDisable() {
        if (vaultManager == null) {
            return;
        }

        for (Player player : getServer().getOnlinePlayers()) {
            vaultManager.saveVault(player.getUniqueId());
        }

        vaultManager.shutdown();
        getServer().getScheduler().cancelTasks(this);
    }

    public PlayerVaultManager getVaultManager() {
        return vaultManager;
    }
}