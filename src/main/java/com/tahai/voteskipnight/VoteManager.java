package com.tahai.voteskipnight;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoteManager {
    private VoteSession currentVote;
    private final Map<UUID, Integer> cooldown = new ConcurrentHashMap<>();
    private final int voteDuration = 400;

    public VoteManager() {
    }

    public boolean initiateVote(UUID initiator, int currentDay) {
        if (currentVote != null) {
            return false;
        }
        Integer lastDay = cooldown.get(initiator);
        if (lastDay != null && lastDay == currentDay) {
            return false;
        }
        currentVote = new VoteSession(initiator);
        cooldown.put(initiator, currentDay);
        Plugin plugin = Bukkit.getPluginManager().getPlugin("VoteSkipNight");
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(plugin, this::handleTimeout, voteDuration);
        }
        return true;
    }

    public boolean addVote(UUID voter, boolean agree) {
        if (currentVote == null) {
            return false;
        }
        if (voter.equals(currentVote.initiator)) {
            return false;
        }
        if (currentVote.agrees.contains(voter) || currentVote.rejects.contains(voter)) {
            return false;
        }
        if (agree) {
            currentVote.agrees.add(voter);
        } else {
            currentVote.rejects.add(voter);
        }
        return true;
    }

    public void handleTimeout() {
        if (currentVote == null) {
            return;
        }
        boolean skip = currentVote.agrees.size() > currentVote.rejects.size();
        if (skip) {
            World world = Bukkit.getWorlds().get(0);
            if (world != null) {
                long time = world.getFullTime();
                long dayTime = (time / 24000) * 24000 + 1000;
                world.setFullTime(dayTime);
            }
        }
        currentVote = null;
    }

    private static class VoteSession {
        final UUID initiator;
        final Set<UUID> agrees = new HashSet<>();
        final Set<UUID> rejects = new HashSet<>();

        VoteSession(UUID initiator) {
            this.initiator = initiator;
        }
    }
}