package com.example.robotvacuumsimulator.model;

public class Cell {
    private final int x;
    private final int y;
    private CellType type;
    private DirtType dirtType;
    private int dirtHealth;
    private boolean visited;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = CellType.EMPTY;
        this.dirtType = DirtType.NONE;
        this.dirtHealth = 0;
        this.visited = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    
    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }
    
    public DirtType getDirtType() { return dirtType; }
    public void setDirtType(DirtType dirtType) { this.dirtType = dirtType; }
    
    public int getDirtHealth() { return dirtHealth; }
    public void setDirtHealth(int dirtHealth) { this.dirtHealth = dirtHealth; }
    public void decreaseDirtHealth() { this.dirtHealth--; }
    
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
}
