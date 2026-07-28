package com.tahai.kuangqu;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionManager {

    public static class Selection {
        private final Location pos1;
        private final Location pos2;
        private final World world;

        public Selection(Location pos1, Location pos2, World world) {
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.world = world;
        }

        public Location getPos1() {
            return pos1;
        }

        public Location getPos2() {
            return pos2;
        }

        public World getWorld() {
            return world;
        }
    }

    private final Map<UUID, Selection> selections = new ConcurrentHashMap<>();

    public void setPos1(UUID playerId, Location pos) {
        Selection old = selections.get(playerId);
        if (old == null) {
            selections.put(playerId, new Selection(pos, null, pos.getWorld()));
        } else {
            World world = pos.getWorld();
            if (!world.equals(old.getWorld())) {
                selections.put(playerId, new Selection(pos, null, world));
            } else {
                selections.put(playerId, new Selection(pos, old.getPos2(), old.getWorld()));
            }
        }
    }

    public void setPos2(UUID playerId, Location pos) {
        Selection old = selections.get(playerId);
        if (old == null) {
            selections.put(playerId, new Selection(null, pos, pos.getWorld()));
        } else {
            World world = pos.getWorld();
            if (!world.equals(old.getWorld())) {
                selections.put(playerId, new Selection(null, pos, world));
            } else {
                selections.put(playerId, new Selection(old.getPos1(), pos, old.getWorld()));
            }
        }
    }

    public Selection getSelection(UUID playerId) {
        return selections.get(playerId);
    }

    public void clearSelection(UUID playerId) {
        selections.remove(playerId);
    }
}