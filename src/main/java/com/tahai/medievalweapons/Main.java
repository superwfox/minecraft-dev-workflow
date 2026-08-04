package com.tahai.medievalweapons;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private WeaponManager weaponManager;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") == null) {
            getLogger().warning("ModelEngine is not loaded. Disabling MedievalWeapons.");
            setEnabled(false);
            return;
        }

        saveDefaultConfig();
        weaponManager = new WeaponManager();
    }

    @Override
    public void onDisable() {
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }
}