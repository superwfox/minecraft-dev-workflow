package com.tahai.supervault;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerVault {

    private final UUID uuid;
    private String type;
    private int amount;
    private ItemStack[] contents;

    public PlayerVault(UUID uuid) {
        this(uuid, "normal", 0, new ItemStack[27]);
    }

    public PlayerVault(UUID uuid, String type, int amount, ItemStack[] contents) {
        this.uuid = uuid;
        this.type = type;
        this.amount = amount;
        this.contents = contents != null ? contents : new ItemStack[27];
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public int getSize() {
        return contents.length;
    }
}