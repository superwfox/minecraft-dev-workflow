package com.tahai.pvpduel;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuelManager {

    private final Map<UUID, DuelInvite> invitations = new ConcurrentHashMap<>();
    private final Set<UUID> duelingPlayers = ConcurrentHashMap.newKeySet();

    public boolean invite(UUID inviter, UUID invitee) {
        if (inviter.equals(invitee)) return false;
        if (isInDuel(inviter) || isInDuel(invitee)) return false;
        return invitations.putIfAbsent(invitee, new DuelInvite(inviter, System.currentTimeMillis())) == null;
    }

    public DuelInvite accept(UUID invitee) {
        DuelInvite invite = invitations.get(invitee);
        if (invite == null) return null;
        if (isInDuel(invitee) || isInDuel(invite.inviter())) return null;
        if (invitations.remove(invitee, invite)) {
            duelingPlayers.add(invite.inviter());
            duelingPlayers.add(invitee);
            return invite;
        }
        return null;
    }

    public boolean removeInvite(UUID invitee) {
        return invitations.remove(invitee) != null;
    }

    public boolean isInDuel(UUID player) {
        return duelingPlayers.contains(player);
    }

    public void removeFromDuel(UUID player) {
        duelingPlayers.remove(player);
    }

    public record DuelInvite(UUID inviter, long createdAt) {
    }
}