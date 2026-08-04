package com.tahai.pvpduel;

import java.util.UUID;

public class DuelInvite {

    private final UUID inviter;
    private final UUID invitee;
    private final long creationTime;

    public DuelInvite(UUID inviter, UUID invitee) {
        this.inviter = inviter;
        this.invitee = invitee;
        this.creationTime = System.currentTimeMillis();
    }

    public UUID getInviter() {
        return inviter;
    }

    public UUID getInvitee() {
        return invitee;
    }

    public long getCreationTime() {
        return creationTime;
    }
}