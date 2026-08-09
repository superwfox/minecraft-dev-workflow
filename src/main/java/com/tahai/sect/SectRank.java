package com.tahai.sect;

public enum SectRank {
    MEMBER("普通成员", "普通成员"),
    ELITE("精英", "精英"),
    VICE_LEADER("副宗主", "副宗主");

    private final String name;
    private final String display;

    SectRank(String name, String display) {
        this.name = name;
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public String getDisplayName() {
        return display;
    }
}