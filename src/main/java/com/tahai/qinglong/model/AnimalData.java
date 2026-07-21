package com.tahai.qinglong.model;

import java.util.List;
import java.util.Map;

public class AnimalData {

    private String key;
    private String displayName;
    private List<String> lore;
    private double scale;
    private Map<String, String> equipment; // key: slot name (hand, head, chest, legs, feet, offhand), value: material name
    private List<SkillData> skills;
    private int unlockLevel;
    private List<UpgradeLevel> upgradeLevels;

    public AnimalData() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Map<String, String> getEquipment() {
        return equipment;
    }

    public void setEquipment(Map<String, String> equipment) {
        this.equipment = equipment;
    }

    public List<SkillData> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillData> skills) {
        this.skills = skills;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public void setUnlockLevel(int unlockLevel) {
        this.unlockLevel = unlockLevel;
    }

    public List<UpgradeLevel> getUpgradeLevels() {
        return upgradeLevels;
    }

    public void setUpgradeLevels(List<UpgradeLevel> upgradeLevels) {
        this.upgradeLevels = upgradeLevels;
    }

    public static class SkillData {
        private String trigger;
        private String particle;
        private double damage;
        private List<EffectData> effects;

        public SkillData() {
        }

        public String getTrigger() {
            return trigger;
        }

        public void setTrigger(String trigger) {
            this.trigger = trigger;
        }

        public String getParticle() {
            return particle;
        }

        public void setParticle(String particle) {
            this.particle = particle;
        }

        public double getDamage() {
            return damage;
        }

        public void setDamage(double damage) {
            this.damage = damage;
        }

        public List<EffectData> getEffects() {
            return effects;
        }

        public void setEffects(List<EffectData> effects) {
            this.effects = effects;
        }
    }

    public static class EffectData {
        private String type;
        private int duration;
        private int amplifier;

        public EffectData() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getAmplifier() {
            return amplifier;
        }

        public void setAmplifier(int amplifier) {
            this.amplifier = amplifier;
        }
    }

    public static class UpgradeLevel {
        private int level;
        private String material;
        private String displayName;
        private List<String> lore;
        private Map<String, Double> attributes;

        public UpgradeLevel() {
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getMaterial() {
            return material;
        }

        public void setMaterial(String material) {
            this.material = material;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public List<String> getLore() {
            return lore;
        }

        public void setLore(List<String> lore) {
            this.lore = lore;
        }

        public Map<String, Double> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, Double> attributes) {
            this.attributes = attributes;
        }
    }
}