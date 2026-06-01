package com.example.robotvacuumsimulator.model;

public enum DirtType {
    NONE("Temiz", 0.0, 0),
    DUST("Toz", 1.0, 1),
    LIQUID("Sıvı", 3.0, 3),
    STAIN("Leke", 5.0, 5);

    private final String name;
    private final double timeMultiplier;
    private final int extraBatteryDrain;

    DirtType(String name, double timeMultiplier, int extraBatteryDrain) {
        this.name = name;
        this.timeMultiplier = timeMultiplier;
        this.extraBatteryDrain = extraBatteryDrain;
    }

    public String getName() { return name; }
    public double getTimeMultiplier() { return timeMultiplier; }
    public int getExtraBatteryDrain() { return extraBatteryDrain; }
}