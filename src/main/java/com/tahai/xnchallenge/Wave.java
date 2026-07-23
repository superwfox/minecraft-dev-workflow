package com.tahai.xnchallenge;

import java.util.List;

public class Wave {
    private final int waveNumber;
    private final List<MobSpawn> mobs;

    public Wave(int waveNumber, List<MobSpawn> mobs) {
        this.waveNumber = waveNumber;
        this.mobs = mobs;
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public List<MobSpawn> getMobs() {
        return mobs;
    }

    public static class MobSpawn {
        private final String type;
        private final int amount;
        private final int delay;

        public MobSpawn(String type, int amount, int delay) {
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
}