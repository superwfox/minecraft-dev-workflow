package com.tahai.xnchallenge;

public class MobConfig {
    private final String type;
    private final int amount;
    private final int delay;

    public MobConfig(String type, int amount, int delay) {
        this.type = type;
        this.amount = amount;
        this.delay = delay;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getDelay() {
        return delay;
    }
}