package com.tahai.anvilplus;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        MaxEnchantCommand maxEnchantCommand = new MaxEnchantCommand();
        getCommand("maxenchant").setExecutor(maxEnchantCommand);
        getCommand("maxenchant").setTabCompleter(maxEnchantCommand);
        getServer().getPluginManager().registerEvents(new AnvilListener(), this);
    }

    @Override
    public void onDisable() {
        // nothing to do
    }
}