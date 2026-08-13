package com.tahai.unpc;

public class NpcData {
    private int id;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private String displayText;
    private String skinPlayerName;
    private String skinTexture;
    private String skinSignature;
    private String command;
    private String commandExecutor;

    public NpcData() {
    }

    public NpcData(int id, String worldName, double x, double y, double z, float yaw, float pitch, String displayText, String skinPlayerName, String skinTexture, String skinSignature, String command, String commandExecutor) {
        this.id = id;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.displayText = displayText;
        this.skinPlayerName = skinPlayerName;
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
        this.command = command;
        this.commandExecutor = commandExecutor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getSkinPlayerName() {
        return skinPlayerName;
    }

    public void setSkinPlayerName(String skinPlayerName) {
        this.skinPlayerName = skinPlayerName;
    }

    public String getSkinTexture() {
        return skinTexture;
    }

    public void setSkinTexture(String skinTexture) {
        this.skinTexture = skinTexture;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public void setSkinSignature(String skinSignature) {
        this.skinSignature = skinSignature;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandExecutor() {
        return commandExecutor;
    }

    public void setCommandExecutor(String commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
}