package com.tahai.authweb.model;

public class ModInfo {
    private String id;
    private String name;
    private String modVersion;
    private String minecraftVersion;
    private String fileName;
    private String filePath;

    public ModInfo() {
    }

    public ModInfo(String id, String name, String modVersion, String minecraftVersion, String fileName, String filePath) {
        this.id = id;
        this.name = name;
        this.modVersion = modVersion;
        this.minecraftVersion = minecraftVersion;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModVersion() {
        return modVersion;
    }

    public void setModVersion(String modVersion) {
        this.modVersion = modVersion;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "ModInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", modVersion='" + modVersion + '\'' +
                ", minecraftVersion='" + minecraftVersion + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}