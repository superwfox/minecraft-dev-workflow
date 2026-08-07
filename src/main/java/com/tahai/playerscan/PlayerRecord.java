package com.tahai.playerscan;

import java.util.LinkedHashSet;
import java.util.Set;

public class PlayerRecord {
    private final String playerName;
    private final Set<String> ips = new LinkedHashSet<>();

    public PlayerRecord(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<String> getIps() {
        return ips;
    }

    public void addIp(String ip) {
        ips.add(ip);
    }
}