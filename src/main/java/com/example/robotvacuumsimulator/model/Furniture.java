package com.example.robotvacuumsimulator.model;

public class Furniture {
    private final int x, y;
    private final FurnitureType type;

    public Furniture(int x, int y, FurnitureType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public FurnitureType getType() { return type; }
}