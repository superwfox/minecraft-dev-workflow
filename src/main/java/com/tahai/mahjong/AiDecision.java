package com.tahai.mahjong;

import java.util.Collections;
import java.util.List;

public class AiDecision {

    public enum ActionType {
        DISCARD, CHI, PON, KAN, RON, TSUMO, PASS
    }

    private final ActionType action;
    private final String tile;
    private final List<String> meldTiles;

    public AiDecision(ActionType action, String tile) {
        this(action, tile, Collections.emptyList());
    }

    public AiDecision(ActionType action, String tile, List<String> meldTiles) {
        this.action = action;
        this.tile = tile;
        this.meldTiles = meldTiles == null ? Collections.emptyList() : meldTiles;
    }

    public ActionType getAction() {
        return action;
    }

    public String getTile() {
        return tile;
    }

    public List<String> getMeldTiles() {
        return meldTiles;
    }
}