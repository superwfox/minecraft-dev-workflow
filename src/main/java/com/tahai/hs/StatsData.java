package com.tahai.hs;

public class StatsData {
    private int recycleCount;
    private double totalCoins;

    public StatsData(int recycleCount, double totalCoins) {
        this.recycleCount = recycleCount;
        this.totalCoins = totalCoins;
    }

    public int getRecycleCount() {
        return recycleCount;
    }

    public double getTotalCoins() {
        return totalCoins;
    }

    public void addRecycle(int count, double coins) {
        this.recycleCount += count;
        this.totalCoins += coins;
    }
}