package com.tahai.kuangqu;

import org.bukkit.Location;

public class Selection {
    private final Location pos1;
    private final Location pos2;

    public Selection(Location pos1, Location pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }
}