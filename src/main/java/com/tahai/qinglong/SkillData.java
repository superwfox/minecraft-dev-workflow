package com.tahai.qinglong;

public class SkillData {
    private String key;
    private String displayName;
    private String description;
    private String type;
    private double damage;
    private int cooldown;
    private double range;

    public SkillData() {}

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }
    public int getCooldown() { return cooldown; }
    public void setCooldown(int cooldown) { this.cooldown = cooldown; }
    public double getRange() { return range; }
    public void setRange(double range) { this.range = range; }
}