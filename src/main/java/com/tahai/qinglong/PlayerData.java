package com.tahai.qinglong;

import java.util.Map;
import java.util.Set;

public class PlayerData {
    private int level;
    private double exp;
    private int qinglongCoins;
    private Set<String> unlockedAnimals;
    private Map<String, Integer> equipmentUpgrades;

    public PlayerData() {
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public int getQinglongCoins() {
        return qinglongCoins;
    }

    public void setQinglongCoins(int qinglongCoins) {
        this.qinglongCoins = qinglongCoins;
    }

    public Set<String> getUnlockedAnimals() {
        return unlockedAnimals;
    }

    public void setUnlockedAnimals(Set<String> unlockedAnimals) {
        this.unlockedAnimals = unlockedAnimals;
    }

    public Map<String, Integer> getEquipmentUpgrades() {
        return equipmentUpgrades;
    }

    public void setEquipmentUpgrades(Map<String, Integer> equipmentUpgrades) {
        this.equipmentUpgrades = equipmentUpgrades;
    }
}