package com.terriblefriends.bookmod;

import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class Attribute {
    private String name;
    private double amount;
    private int operation;
    private long uuidLow;
    private long uuidHigh;

    public Attribute(String name, double amount, int operation) {
        this.name = name;
        this.amount = amount;
        this.operation = operation;

        UUID randomUuid = UUID.randomUUID();
        this.uuidHigh = randomUuid.getMostSignificantBits();
        this.uuidLow = randomUuid.getLeastSignificantBits();
    }

    public NbtCompound writeToTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("AttributeName", name);
        tag.putString("Name", name);
        tag.putDouble("Amount", amount);
        tag.putInt("Operation", operation);
        tag.putLong("UUIDMost", uuidHigh);
        tag.putLong("UUIDLeast", uuidLow);

        return tag;
    }
}
