package com.example.robotvacuumsimulator.model;

public class Robot {
    private int x, y;
    private double battery;
    private String direction; // "Kuzey", "Güney", "Doğu", "Batı"

    public Robot(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.battery = 100.0;
        this.direction = "Doğu";
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public double getBattery() { return battery; }
    public String getDirection() { return direction; }

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

    public void move(int newX, int newY, DirtType dirtType) {
        if (battery > 0) {
            this.x = newX;
            this.y = newY;

            // Temel hareket pil tüketimi + kir tipine göre ek tüketim
            double drain = 0.5 + dirtType.getExtraBatteryDrain() * 0.2;
            this.battery = Math.max(0.0, this.battery - drain);
        }
    }
}