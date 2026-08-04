package com.tahai.medievalweapons;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerRecipes();
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    @Override
    public void onDisable() {
    }

    private void registerRecipes() {
        getServer().addRecipe(createRecipe("greatsword", 1, "III", "III", " S "));
        getServer().addRecipe(createRecipe("mace", 2, "II ", "II ", " S "));
    }

    private ShapedRecipe createRecipe(String key, int customModelData, String... shape) {
        ItemStack result = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = result.getItemMeta();
        meta.setCustomModelData(customModelData);
        result.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, key), result);
        recipe.shape(shape);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }
}