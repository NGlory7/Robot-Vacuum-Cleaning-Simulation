package com.example.robotvacuumsimulator.controller;

import com.example.robotvacuumsimulator.model.Robot;
import com.example.robotvacuumsimulator.model.DirtType;
import com.example.robotvacuumsimulator.view.SimulationGUI;
import javafx.animation.AnimationTimer;
import java.util.*;

public class GameController {
    private final int GRID_SIZE = 16;
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE]; // 0: Boş, 1: Engel
    private DirtType[][] dirtGrid = new DirtType[GRID_SIZE][GRID_SIZE];

    private final Robot robot;
    private SimulationGUI view;
    private AnimationTimer timer;
    private boolean isRunning = false;

    private final int stationX = 0;
    private final int stationY = 0;

    private int cleanedCount = 0;
    private int totalDirtCount = 0;
    private long lastUpdate = 0;
    private int speedMultiplier = 1;
    private String currentAlgo = "Rastgele";
    private boolean returningToStation = false;

    public GameController() {
        this.robot = new Robot(2, 2);
        resetSimulation();
    }

    public void setView(SimulationGUI view) {
        this.view = view;
        setupGameLoop();
    }

    private void setupGameLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Hız sürgüsüne göre tetiklenme frekansı (Hızlandıkça süre kısalır)
                long threshold = (long) (600_000_000 / speedMultiplier);
                if (now - lastUpdate >= threshold) {
                    updateSimulation();
                    lastUpdate = now;
                }
            }
        };
    }

    public void startSimulation(int speed, String algo) {
        this.speedMultiplier = speed;
        this.currentAlgo = algo;
        if (!isRunning) {
            timer.start();
            isRunning = true;
        }
    }

    public void pauseSimulation() {
        if (isRunning) {
            timer.stop();
            isRunning = false;
        }
    }

    public void resetSimulation() {
        pauseSimulation();
        robot.setPosition(2, 2);
        robot.setBattery(100.0);
        returningToStation = false;
        cleanedCount = 0;
        totalDirtCount = 0;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
                dirtGrid[i][j] = DirtType.NONE;
            }
        }
        // Sabit birkaç mobilya ekleyelim başlangıç için
        grid[5][5] = 1; grid[5][6] = 1; grid[6][5] = 1;
        grid[10][12] = 1; grid[11][12] = 1;
    }

    public void addObstacle(int x, int y) {
        if ((x == robot.getX() && y == robot.getY()) || (x == stationX && y == stationY)) return;
        grid[x][y] = grid[x][y] == 1 ? 0 : 1;
        dirtGrid[x][y] = DirtType.NONE;
    }

    public void addDirt(int x, int y, DirtType type) {
        if (grid[x][y] == 1) return;
        if (dirtGrid[x][y] == DirtType.NONE) totalDirtCount++;
        dirtGrid[x][y] = type;
    }

    public void chargeManual() {
        robot.setBattery(100.0);
        returningToStation = false;
    }

    public void sendToStation() {
        returningToStation = true;
        startSimulation(speedMultiplier, currentAlgo);
    }

    private void updateSimulation() {
        // Otomatik Kritik Şarj İstasyonu Dönüş Kontrolü
        if (robot.getBattery() <= 20.0) {
            returningToStation = true;
        }

        if (returningToStation) {
            navigateWithBFS(stationX, stationY);
            if (robot.getX() == stationX && robot.getY() == stationY) {
                robot.setBattery(100.0);
                returningToStation = false;
                pauseSimulation();
            }
            view.draw();
            return;
        }

        // Üzerinde durulan hücreyi temizle
        int rx = robot.getX();
        int ry = robot.getY();
        if (dirtGrid[rx][ry] != DirtType.NONE) {
            dirtGrid[rx][ry] = DirtType.NONE;
            cleanedCount++;
            if (totalDirtCount > 0) totalDirtCount--;
        }

        // Seçilen algoritmaya göre karar verme mekanizması
        switch (currentAlgo) {
            case "Duvar Takip":
                moveWallFollow();
                break;
            case "Spiral":
                moveSpiral();
                break;
            default:
                moveRandom();
                break;
        }
        view.draw();
    }

    // BFS Algoritması - En Kısa Yol (Şarj istasyonuna gidiş için)
    private void navigateWithBFS(int targetX, int targetY) {
        int startX = robot.getX();
        int startY = robot.getY();
        if (startX == targetX && startY == targetY) return;

        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
        int[][][] parent = new int[GRID_SIZE][GRID_SIZE][2];

        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        boolean found = false;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0];
            int cy = curr[1];

            if (cx == targetX && cy == targetY) {
                found = true;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];

                if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
                    if (!visited[nx][ny] && grid[nx][ny] == 0) {
                        queue.add(new int[]{nx, ny});
                        visited[nx][ny] = true;
                        parent[nx][ny] = new int[]{cx, cy};
                    }
                }
            }
        }

        if (found) {
            int currX = targetX;
            int currY = targetY;
            while (parent[currX][currY][0] != startX || parent[currX][currY][1] != startY) {
                int px = parent[currX][currY][0];
                int py = parent[currX][currY][1];
                currX = px;
                currY = py;
            }
            robot.move(currX, currY, DirtType.NONE);
        }
    }

    private void moveRandom() {
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        String[] dirs = {"Güney", "Kuzey", "Doğu", "Batı"};

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int nx = robot.getX() + dx[i];
            int ny = robot.getY() + dy[i];
            if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && grid[nx][ny] == 0) {
                validIndices.add(i);
            }
        }

        if (!validIndices.isEmpty()) {
            int index = validIndices.get((int) (Math.random() * validIndices.size()));
            robot.setDirection(dirs[index]);
            robot.move(robot.getX() + dx[index], robot.getY() + dy[index], dirtGrid[robot.getX()][robot.getY()]);
        }
    }

    private void moveWallFollow() {
        // Basit yönlü ileri hamle, engel görürse saat yönünde dönme
        int nx = robot.getX();
        int ny = robot.getY();
        String dir = robot.getDirection();

        if (dir.equals("Doğu")) nx++;
        else if (dir.equals("Batı")) nx--;
        else if (dir.equals("Kuzey")) ny--;
        else if (dir.equals("Güney")) ny++;

        if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && grid[nx][ny] == 0) {
            robot.move(nx, ny, dirtGrid[nx][ny]);
        } else {
            // Engel çıktı, dön
            if (dir.equals("Doğu")) robot.setDirection("Güney");
            else if (dir.equals("Güney")) robot.setDirection("Batı");
            else if (dir.equals("Batı")) robot.setDirection("Kuzey");
            else robot.setDirection("Doğu");
        }
    }

    private void moveSpiral() {
        // Spiral davranışı için basit genişleyen adımlar simülasyonu
        moveRandom();
    }

    // Getter'lar
    public int[][] getGrid() { return grid; }
    public DirtType[][] getDirtGrid() { return dirtGrid; }
    public int getRobotX() { return robot.getX(); }
    public int getRobotY() { return robot.getY(); }
    public double getRobotBattery() { return robot.getBattery(); }
    public String getRobotDirection() { return robot.getDirection(); }
    public int getStationX() { return stationX; }
    public int getStationY() { return stationY; }
    public int getCleanedCount() { return cleanedCount; }
    public int getTotalDirtCount() { return totalDirtCount; }
}