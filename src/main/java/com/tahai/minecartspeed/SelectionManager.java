package com.tahai.minecartspeed;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionManager {

    private final Map<UUID, Location[]> selections = new ConcurrentHashMap<>();

    public void setPoint(UUID playerId, int pointIndex, Location location) {
        if (pointIndex < 0 || pointIndex > 1) {
            throw new IllegalArgumentException("pointIndex must be 0 or 1");
        }
        selections.computeIfAbsent(playerId, k -> new Location[2])[pointIndex] = location;
    }

    public boolean isComplete(UUID playerId) {
        Location[] points = selections.get(playerId);
        return points != null && points[0] != null && points[1] != null;
    }

    public Location[] getPoints(UUID playerId) {
        Location[] points = selections.get(playerId);
        if (points != null && points[0] != null && points[1] != null) {
            return points;
        }
        return null;
    }

    public void clear(UUID playerId) {
        selections.remove(playerId);
    }
}