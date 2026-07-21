package com.tahai.lottery;

import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Map;

public class BoxData {
    private final String id;
    private final Map<String, Double> probabilities;
    private final Map<String, List<ItemStack>> rewards;

    public BoxData(String id, Map<String, Double> probabilities, Map<String, List<ItemStack>> rewards) {
        this.id = id;
        this.probabilities = probabilities;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public Map<String, Double> getProbabilities() {
        return probabilities;
    }

    public Map<String, List<ItemStack>> getRewards() {
        return rewards;
    }
}