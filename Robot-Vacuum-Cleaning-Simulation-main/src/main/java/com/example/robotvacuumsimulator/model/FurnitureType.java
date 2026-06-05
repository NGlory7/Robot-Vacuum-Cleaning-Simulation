package com.example.robotvacuumsimulator.model;

public enum FurnitureType {
    PLANT("Bitki (1x1)", 1, 1),
    ARMCHAIR("Tekli Koltuk (2x2)", 2, 2),
    SOFA("Üçlü Koltuk (4x2)", 4, 2),
    TABLE("Yemek Masası (3x3)", 3, 3),
    TV_UNIT("TV Ünitesi (1x4)", 1, 4),
    CARPET("Halı (4x3)", 4, 3); // YENİ: Sadece standart Halı kaldı

    private final String name;
    private final int width;
    private final int height;

    FurnitureType(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    @Override
    public String toString() {
        return name;
    }
}