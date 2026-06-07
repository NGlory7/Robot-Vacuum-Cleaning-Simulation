package com.example.robotvacuumsimulator.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GridMap {
    private final int size;
    private final Cell[][] cells;
    private final List<Furniture> furnitureList;
    private int totalDirtCount;
    private int cleanedCount;
    private final int stationX;
    private final int stationY;

    public GridMap(int size, int stationX, int stationY) {
        this.size = size;
        this.stationX = stationX;
        this.stationY = stationY;
        this.cells = new Cell[size][size];
        this.furnitureList = new ArrayList<>();
        this.totalDirtCount = 0;
        this.cleanedCount = 0;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }

    public void reset() {
        furnitureList.clear();
        totalDirtCount = 0;
        cleanedCount = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].setType(CellType.EMPTY);
                cells[i][j].setDirtType(DirtType.NONE);
                cells[i][j].setDirtHealth(0);
                cells[i][j].setVisited(false);
            }
        }
    }

    public Cell getCell(int x, int y) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            return cells[x][y];
        }
        return null;
    }

    public int getSize() {
        return size;
    }

    public List<Furniture> getFurnitureList() {
        return furnitureList;
    }

    public int getTotalDirtCount() {
        return totalDirtCount;
    }
    
    public void decrementTotalDirtCount() {
        if (this.totalDirtCount > 0) this.totalDirtCount--;
    }

    public int getCleanedCount() {
        return cleanedCount;
    }
    
    public void incrementCleanedCount() {
        this.cleanedCount++;
    }

    public boolean addFurniture(int startX, int startY, FurnitureType type, Robot robot) {
        int w = type.getWidth();
        int h = type.getHeight();
        if (startX + w > size || startY + h > size) return false;

        for (int i = startX; i < startX + w; i++) {
            for (int j = startY; j < startY + h; j++) {
                if (cells[i][j].getType() != CellType.EMPTY || (i == robot.getX() && j == robot.getY()) || (i == stationX && j == stationY)) {
                    return false;
                }
            }
        }

        for (int i = startX; i < startX + w; i++) {
            for (int j = startY; j < startY + h; j++) {
                if (type == FurnitureType.CARPET) {
                    cells[i][j].setType(CellType.CARPET);
                } else {
                    cells[i][j].setType(CellType.SOLID);
                    if (cells[i][j].getDirtType() != DirtType.NONE) {
                        cells[i][j].setDirtType(DirtType.NONE);
                        cells[i][j].setDirtHealth(0);
                        decrementTotalDirtCount();
                    }
                }
            }
        }
        furnitureList.add(new Furniture(startX, startY, type));
        return true;
    }

    public boolean addDirt(int x, int y, DirtType type) {
        Cell cell = getCell(x, y);
        if (cell == null || cell.getType() == CellType.SOLID) return false;
        
        if (cell.getDirtType() == DirtType.NONE) totalDirtCount++;
        cell.setDirtType(type);

        if (type == DirtType.STAIN) cell.setDirtHealth(4);
        else if (type == DirtType.LIQUID) cell.setDirtHealth(2);
        else cell.setDirtHealth(1);

        cell.setVisited(false);
        return true;
    }

    public boolean removeEntity(int x, int y) {
        Iterator<Furniture> it = furnitureList.iterator();
        while (it.hasNext()) {
            Furniture f = it.next();
            if (x >= f.getX() && x < f.getX() + f.getType().getWidth() &&
                    y >= f.getY() && y < f.getY() + f.getType().getHeight()) {

                for (int i = f.getX(); i < f.getX() + f.getType().getWidth(); i++) {
                    for (int j = f.getY(); j < f.getY() + f.getType().getHeight(); j++) {
                        cells[i][j].setType(CellType.EMPTY);
                    }
                }
                it.remove();
                return true;
            }
        }
        
        Cell cell = getCell(x, y);
        if (cell != null && cell.getDirtType() != DirtType.NONE) {
            cell.setDirtType(DirtType.NONE);
            cell.setDirtHealth(0);
            decrementTotalDirtCount();
            return true;
        }
        
        return false;
    }
}
