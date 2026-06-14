package com.example.coppersword;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemMeta;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ConfigManager configManager;
    private CooldownManager cooldownManager;

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        configManager = new ConfigManager();
        cooldownManager = new CooldownManager();

        CopperSwordCommand commandExecutor = new CopperSwordCommand();
        getCommand("coppersword").setExecutor(commandExecutor);
        getCommand("coppersword").setTabCompleter(commandExecutor);

        getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
        getServer().getPluginManager().registerEvents(new CoolDownGuiClickListener(), this);

        ItemStack copperSword = new ItemStack(Material.STONE_SWORD);
        ItemMeta meta = copperSword.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "铜剑");
        copperSword.setItemMeta(meta);
        NamespacedKey key = new NamespacedKey(this, "copper_sword");
        ShapedRecipe recipe = new ShapedRecipe(key, copperSword);
        recipe.shape("I", "I", "S");
        recipe.setIngredient('I', Material.COPPER_INGOT);
        recipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(recipe);
    }

    @Override
    public void onDisable() {
        cooldownManager.save();
        getServer().getScheduler().cancelTasks(this);
    }
}