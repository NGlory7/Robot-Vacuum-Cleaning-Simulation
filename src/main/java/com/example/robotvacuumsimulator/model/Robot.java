package com.example.robotvacuumsimulator.model;

public class Robot {
    private int x, y;
    private double battery;
    private String direction; // "Kuzey", "Güney", "Doğu", "Batı"

    private int currentDust;
    private final int MAX_DUST_CAPACITY = 20;

    public Robot(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.battery = 100.0;
        this.direction = "Doğu";
        this.currentDust = 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public double getBattery() { return battery; }
    public String getDirection() { return direction; }

    public boolean isDustbinFull() { return currentDust >= MAX_DUST_CAPACITY; }
    public double getDustbinPercentage() { return ((double) currentDust / MAX_DUST_CAPACITY) * 100.0; }

    public void setBattery(double battery) {
        this.battery = Math.min(100.0, Math.max(0.0, battery));
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void addDust(int amount) {
        this.currentDust = Math.min(this.currentDust + amount, MAX_DUST_CAPACITY);
    }

    public void emptyDustbin() {
        this.currentDust = 0;
    }

    public void move(int newX, int newY, DirtType dirtType) {
        if (battery > 0) {
            this.x = newX;
            this.y = newY;
            double drain = 0.5 + (dirtType != null ? dirtType.getExtraBatteryDrain() * 0.2 : 0);
            this.battery = Math.max(0.0, this.battery - drain);
        }
    }
}