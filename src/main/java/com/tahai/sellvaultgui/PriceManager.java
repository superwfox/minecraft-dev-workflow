package com.tahai.sellvaultgui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class PriceManager {

    private final Plugin plugin;
    private final Map<Material, Double> prices = new HashMap<>();

    public PriceManager(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        prices.clear();
        FileConfiguration config = plugin.getConfig();

        Map<String, Double> rarityPrices = new HashMap<>();
        ConfigurationSection raritySection = config.getConfigurationSection("rarity-prices");
        if (raritySection != null) {
            for (String key : raritySection.getKeys(false)) {
                rarityPrices.put(key.toUpperCase(), raritySection.getDouble(key));
            }
        }

        ConfigurationSection itemSection = config.getConfigurationSection("item-prices");
        if (itemSection != null) {
            for (String key : itemSection.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material == null) continue;
                Object value = itemSection.get(key);
                if (value instanceof Number) {
                    prices.put(material, ((Number) value).doubleValue());
                } else if (value instanceof ConfigurationSection) {
                    ConfigurationSection item = (ConfigurationSection) value;
                    double price = 0.0;
                    boolean found = false;
                    Object priceObj = item.get("price");
                    if (priceObj instanceof Number) {
                        price = ((Number) priceObj).doubleValue();
                        found = true;
                    }
                    String rarity = item.getString("rarity");
                    if (!found && rarity != null) {
                        Double rarityPrice = rarityPrices.get(rarity.toUpperCase());
                        if (rarityPrice != null) {
                            price = rarityPrice;
                            found = true;
                        }
                    }
                    if (found) {
                        prices.put(material, price);
                    }
                }
            }
        }
    }

    public double getPrice(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0.0;
        Double price = prices.get(item.getType());
        return price == null ? 0.0 : price * item.getAmount();
    }
}