package com.tahai.authwebmanager;

public class ModInfo {
    private String name;
    private String version;
    private String core;

    public ModInfo() {
    }

    public ModInfo(String name, String version, String core) {
        this.name = name;
        this.version = version;
        this.core = core;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCore() {
        return core;
    }

    public void setCore(String core) {
        this.core = core;
    }
}