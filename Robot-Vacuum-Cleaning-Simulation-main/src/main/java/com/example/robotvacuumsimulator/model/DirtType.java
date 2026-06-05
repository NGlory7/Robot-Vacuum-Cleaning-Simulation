package com.example.robotvacuumsimulator.model;

public enum DirtType {
    NONE("Temiz", 0),
    DUST("Toz", 1),
    LIQUID("Sıvı", 3),
    STAIN("Leke", 5);

    private final String name;
    private final int extraBatteryDrain;

    DirtType(String name, int extraBatteryDrain) {
        this.name = name;
        this.extraBatteryDrain = extraBatteryDrain;
    }

    public String getName() { return name; }
    public int getExtraBatteryDrain() { return extraBatteryDrain; }
}