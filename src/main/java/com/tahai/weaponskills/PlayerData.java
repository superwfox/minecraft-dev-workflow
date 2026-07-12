package com.tahai.weaponskills;

import java.util.Objects;
import java.util.UUID;

public class PlayerData {
    private String currentSkill;
    private int comboCount;
    private UUID comboTarget;
    private boolean swordActive;
    private boolean swordCooldownFlag;
    private int axeShieldBreakCount;
    private boolean axeReady;
    private int hammerHeavyCount;
    private boolean hammerReady;
    private int frostValue;

    public PlayerData() {
    }

    public PlayerData(String currentSkill, int comboCount, UUID comboTarget, boolean swordActive,
                      boolean swordCooldownFlag, int axeShieldBreakCount, boolean axeReady,
                      int hammerHeavyCount, boolean hammerReady, int frostValue) {
        this.currentSkill = currentSkill;
        this.comboCount = comboCount;
        this.comboTarget = comboTarget;
        this.swordActive = swordActive;
        this.swordCooldownFlag = swordCooldownFlag;
        this.axeShieldBreakCount = axeShieldBreakCount;
        this.axeReady = axeReady;
        this.hammerHeavyCount = hammerHeavyCount;
        this.hammerReady = hammerReady;
        this.frostValue = frostValue;
    }

    public String getCurrentSkill() {
        return currentSkill;
    }

    public void setCurrentSkill(String currentSkill) {
        this.currentSkill = currentSkill;
    }

    public int getComboCount() {
        return comboCount;
    }

    public void setComboCount(int comboCount) {
        this.comboCount = comboCount;
    }

    public UUID getComboTarget() {
        return comboTarget;
    }

    public void setComboTarget(UUID comboTarget) {
        this.comboTarget = comboTarget;
    }

    public boolean isSwordActive() {
        return swordActive;
    }

    public void setSwordActive(boolean swordActive) {
        this.swordActive = swordActive;
    }

    public boolean isSwordCooldownFlag() {
        return swordCooldownFlag;
    }

    public void setSwordCooldownFlag(boolean swordCooldownFlag) {
        this.swordCooldownFlag = swordCooldownFlag;
    }

    public int getAxeShieldBreakCount() {
        return axeShieldBreakCount;
    }

    public void setAxeShieldBreakCount(int axeShieldBreakCount) {
        this.axeShieldBreakCount = axeShieldBreakCount;
    }

    public boolean isAxeReady() {
        return axeReady;
    }

    public void setAxeReady(boolean axeReady) {
        this.axeReady = axeReady;
    }

    public int getHammerHeavyCount() {
        return hammerHeavyCount;
    }

    public void setHammerHeavyCount(int hammerHeavyCount) {
        this.hammerHeavyCount = hammerHeavyCount;
    }

    public boolean isHammerReady() {
        return hammerReady;
    }

    public void setHammerReady(boolean hammerReady) {
        this.hammerReady = hammerReady;
    }

    public int getFrostValue() {
        return frostValue;
    }

    public void setFrostValue(int frostValue) {
        this.frostValue = frostValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return comboCount == that.comboCount &&
                swordActive == that.swordActive &&
                swordCooldownFlag == that.swordCooldownFlag &&
                axeShieldBreakCount == that.axeShieldBreakCount &&
                axeReady == that.axeReady &&
                hammerHeavyCount == that.hammerHeavyCount &&
                hammerReady == that.hammerReady &&
                frostValue == that.frostValue &&
                Objects.equals(currentSkill, that.currentSkill) &&
                Objects.equals(comboTarget, that.comboTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentSkill, comboCount, comboTarget, swordActive, swordCooldownFlag,
                axeShieldBreakCount, axeReady, hammerHeavyCount, hammerReady, frostValue);
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "currentSkill='" + currentSkill + '\'' +
                ", comboCount=" + comboCount +
                ", comboTarget=" + comboTarget +
                ", swordActive=" + swordActive +
                ", swordCooldownFlag=" + swordCooldownFlag +
                ", axeShieldBreakCount=" + axeShieldBreakCount +
                ", axeReady=" + axeReady +
                ", hammerHeavyCount=" + hammerHeavyCount +
                ", hammerReady=" + hammerReady +
                ", frostValue=" + frostValue +
                '}';
    }
}