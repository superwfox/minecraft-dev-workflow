package com.tahai.sellvaultgui;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private PriceManager priceManager;
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Vault economy provider not found. Disabling SellVaultGui.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();

        priceManager = new PriceManager(this);
        priceManager.reload();

        SellCommands commands = new SellCommands();
        getCommand("sellgui").setExecutor(commands);
        getCommand("sellgui").setTabCompleter(commands);
        getCommand("reloadsellgui").setExecutor(commands);
        getCommand("reloadsellgui").setTabCompleter(commands);

        getServer().getPluginManager().registerEvents(new SellGuiListener(this, priceManager), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public Economy getEconomy() {
        return economy;
    }

    public PriceManager getPriceManager() {
        return priceManager;
    }
}