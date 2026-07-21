package com.tahai.qinglong;

public class UpgradeLevel {
    private int level;
    private double cost;
    private double damage;
    private double health;
    private double speed;

    public UpgradeLevel() {}

    public UpgradeLevel(int level, double cost, double damage, double health, double speed) {
        this.level = level;
        this.cost = cost;
        this.damage = damage;
        this.health = health;
        this.speed = speed;
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }
    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
}