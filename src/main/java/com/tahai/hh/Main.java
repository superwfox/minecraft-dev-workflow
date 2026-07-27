package com.tahai.hh;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.tahai.hh.GuiListener;
import com.tahai.hh.HhCommand;

public class Main extends JavaPlugin {
    public static Economy economy;
    public static PlayerPoints playerPoints;

    public static Economy getEconomy() {
        return economy;
    }

    public static PlayerPoints getPlayerPoints() {
        return playerPoints;
    }

    @Override
    public void onEnable() {
        // 获取Vault Economy
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        } else {
            getLogger().warning("Vault economy not found. Economy features disabled.");
        }

        // 获取PlayerPoints
        Plugin pp = getServer().getPluginManager().getPlugin("PlayerPoints");
        if (pp instanceof PlayerPoints) {
            playerPoints = (PlayerPoints) pp;
        } else {
            getLogger().warning("PlayerPoints not found. Points features disabled.");
        }

        // 注册命令和Tab补全
        HhCommand cmd = new HhCommand();
        getCommand("hh").setExecutor(cmd);
        getCommand("hh").setTabCompleter(cmd);

        // 注册监听器
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}