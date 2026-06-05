package com.example.robotvacuumsimulator.controller;

import com.example.robotvacuumsimulator.model.Robot;
import com.example.robotvacuumsimulator.model.DirtType;
import com.example.robotvacuumsimulator.model.Furniture;
import com.example.robotvacuumsimulator.model.FurnitureType;
import com.example.robotvacuumsimulator.view.SimulationGUI;
import javafx.animation.AnimationTimer;
import java.util.*;

public class GameController {
    private final int GRID_SIZE = 16;
    // 0: Boş, 1: Mobilya, 2: Büyük Halı (Yüksek Güç), 3: Küçük Halı (Normal Güç)
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private DirtType[][] dirtGrid = new DirtType[GRID_SIZE][GRID_SIZE];
    private int[][] dirtHealth = new int[GRID_SIZE][GRID_SIZE];

    private boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
    private String sweepDirection = "Doğu";
    private boolean isResuming = false;
    private int resumeX = -1;
    private int resumeY = -1;
    private boolean missionCompleted = false;
    private boolean isCharging = false;
    
    // BFS Optimizasyonu için Rota Kuyruğu
    private Queue<int[]> currentPath = new LinkedList<>();

    // Spiral Algoritması Değişkenleri
    private int spiralStepsTaken = 0;
    private int spiralMaxSteps = 1;
    private int spiralTurns = 0;
    private int spiralDirectionIndex = 0; // 0: Doğu, 1: Güney, 2: Batı, 3: Kuzey
    private final String[] spiralDirs = {"Doğu", "Güney", "Batı", "Kuzey"};
    private final int[] spiralDx = {1, 0, -1, 0};
    private final int[] spiralDy = {0, 1, 0, -1};

    private List<Furniture> furnitureList = new ArrayList<>();
    private final Robot robot;
    private SimulationGUI view;
    private AnimationTimer timer;
    private boolean isRunning = false;

    private final int stationX = 0;
    private final int stationY = 0;

    private int cleanedCount = 0;
    private int totalDirtCount = 0;
    private long lastUpdate = 0;
    private long lastSecondTick = 0;
    private int elapsedSeconds = 0;

    private int speedMultiplier = 1;
    private String currentAlgo = "Akıllı Rota";
    private boolean returningToStation = false;

    public GameController() {
        this.robot = new Robot(stationX, stationY);
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
                if (now - lastSecondTick >= 1_000_000_000) {
                    elapsedSeconds++;
                    lastSecondTick = now;
                    view.draw();
                }
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
        if (missionCompleted && totalDirtCount > 0) missionCompleted = false;

        if (!isRunning) {
            lastSecondTick = System.nanoTime();
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
        robot.setPosition(stationX, stationY);
        robot.setBattery(100.0);
        robot.emptyDustbin();
        robot.emptyLiquidTank(); // YENİ
        returningToStation = false;
        isResuming = false;
        isCharging = false;
        missionCompleted = false;
        resumeX = -1;
        resumeY = -1;
        sweepDirection = "Doğu";
        cleanedCount = 0;
        totalDirtCount = 0;
        elapsedSeconds = 0;
        furnitureList.clear();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
                dirtGrid[i][j] = DirtType.NONE;
                dirtHealth[i][j] = 0;
                visited[i][j] = false;
            }
        }

        addFurniture(11, 2, FurnitureType.CARPET_LARGE);
        addFurniture(3, 7, FurnitureType.CARPET_SMALL);
        addFurniture(5, 2, FurnitureType.SOFA);
        addFurniture(12, 5, FurnitureType.TABLE);
        addFurniture(2, 8, FurnitureType.ARMCHAIR);
    }

    public boolean addFurniture(int startX, int startY, FurnitureType type) {
        int w = type.getWidth();
        int h = type.getHeight();
        if (startX + w > GRID_SIZE || startY + h > GRID_SIZE) return false;

        for (int i = startX; i < startX + w; i++) {
            for (int j = startY; j < startY + h; j++) {
                if (grid[i][j] == 1 || (i == robot.getX() && j == robot.getY()) || (i == stationX && j == stationY)) {
                    return false;
                }
            }
        }

        for (int i = startX; i < startX + w; i++) {
            for (int j = startY; j < startY + h; j++) {
                // YENİ: Küçük halı (3) ve Büyük halı (2) ayrımı
                if (type == FurnitureType.CARPET_LARGE) {
                    grid[i][j] = 2;
                } else if (type == FurnitureType.CARPET_SMALL) {
                    grid[i][j] = 3;
                } else {
                    grid[i][j] = 1;
                    if (dirtGrid[i][j] != DirtType.NONE) {
                        dirtGrid[i][j] = DirtType.NONE;
                        dirtHealth[i][j] = 0;
                        if (totalDirtCount > 0) totalDirtCount--;
                    }
                }
            }
        }
        furnitureList.add(new Furniture(startX, startY, type));
        return true;
    }

    public void addDirt(int x, int y, DirtType type) {
        if (grid[x][y] == 1) return;
        if (dirtGrid[x][y] == DirtType.NONE) totalDirtCount++;
        dirtGrid[x][y] = type;

        if (type == DirtType.STAIN) dirtHealth[x][y] = 3 + (int)(Math.random() * 3);
        else dirtHealth[x][y] = 1;

        visited[x][y] = false;
        missionCompleted = false;
    }

    public void removeEntity(int x, int y) {
        Iterator<Furniture> it = furnitureList.iterator();
        while (it.hasNext()) {
            Furniture f = it.next();
            if (x >= f.getX() && x < f.getX() + f.getType().getWidth() &&
                    y >= f.getY() && y < f.getY() + f.getType().getHeight()) {

                for (int i = f.getX(); i < f.getX() + f.getType().getWidth(); i++) {
                    for (int j = f.getY(); j < f.getY() + f.getType().getHeight(); j++) {
                        grid[i][j] = 0;
                    }
                }
                it.remove();
                return;
            }
        }
        if (dirtGrid[x][y] != DirtType.NONE) {
            dirtGrid[x][y] = DirtType.NONE;
            dirtHealth[x][y] = 0;
            if (totalDirtCount > 0) totalDirtCount--;
        }
    }

    public void sendToStation() {
        if (!returningToStation && !isResuming && !isCharging) {
            resumeX = robot.getX();
            resumeY = robot.getY();
        }
        returningToStation = true;
        isResuming = false;
        startSimulation(speedMultiplier, currentAlgo);
    }

    private void updateSimulation() {
        if (isCharging) {
            double newBat = robot.getBattery() + 2.5;
            if (newBat >= 100.0) {
                robot.setBattery(100.0);
                isCharging = false;
                if (missionCompleted) pauseSimulation();
                else if (resumeX != -1 && resumeY != -1 && (resumeX != stationX || resumeY != stationY)) isResuming = true;
                else pauseSimulation();
            } else {
                robot.setBattery(newBat);
            }
            view.draw();
            return;
        }

        if (!missionCompleted && totalDirtCount == 0) {
            missionCompleted = true;
            returningToStation = true;
            isResuming = false;
            resumeX = -1;
            resumeY = -1;
        }

        // YENİ: Sıvı haznesi de dolduğunda istasyona dönmeli
        if (!returningToStation && !isResuming && !missionCompleted &&
                (robot.getBattery() <= 20.0 || robot.isDustbinFull() || robot.isLiquidTankFull())) {
            resumeX = robot.getX();
            resumeY = robot.getY();
            returningToStation = true;
        }

        if (returningToStation) {
            navigateWithBFS(stationX, stationY);
            if (robot.getX() == stationX && robot.getY() == stationY) {
                robot.emptyDustbin();
                robot.emptyLiquidTank(); // YENİ
                returningToStation = false;
                isCharging = true;
            }
            view.draw();
            return;
        }

        if (isResuming) {
            navigateWithBFS(resumeX, resumeY);
            if (robot.getX() == resumeX && robot.getY() == resumeY) {
                isResuming = false;
                resumeX = -1;
                resumeY = -1;
            }
            view.draw();
            return;
        }

        int rx = robot.getX();
        int ry = robot.getY();
        visited[rx][ry] = true;

        // Sadece Büyük Halıdaysa (2) yüksek güç tüket
        if (grid[rx][ry] == 2) robot.setBattery(robot.getBattery() - 1.2);

        DirtType currentDirt = dirtGrid[rx][ry];
        if (currentDirt != DirtType.NONE) {

            // YENİ: Hazne ayrıştırması
            if (currentDirt == DirtType.STAIN) {
                dirtHealth[rx][ry]--;
                robot.addDust(1);
                if (dirtHealth[rx][ry] <= 0) {
                    dirtGrid[rx][ry] = DirtType.NONE;
                    cleanedCount++;
                    if (totalDirtCount > 0) totalDirtCount--;
                } else {
                    view.draw();
                    return;
                }
            } else if (currentDirt == DirtType.LIQUID) {
                robot.addLiquid(2); // Sıvıysa sıvı haznesine gider
                dirtGrid[rx][ry] = DirtType.NONE;
                cleanedCount++;
                if (totalDirtCount > 0) totalDirtCount--;
            } else {
                robot.addDust(1); // Tozsa toz haznesine
                dirtGrid[rx][ry] = DirtType.NONE;
                cleanedCount++;
                if (totalDirtCount > 0) totalDirtCount--;
            }
        }

        switch (currentAlgo) {
            case "Akıllı Rota": moveSmart(); break;
            case "Duvar Takip": moveWallFollow(); break;
            case "Spiral": moveSpiral(); break;
            default: moveRandom(); break;
        }
        view.draw();
    }

    private void moveSmart() {
        int rx = robot.getX(); int ry = robot.getY(); int nx = rx, ny = ry;
        if (sweepDirection.equals("Doğu")) nx++; else if (sweepDirection.equals("Batı")) nx--;

        if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && grid[nx][ny] != 1 && !visited[nx][ny]) {
            robot.setDirection(sweepDirection);
            robot.move(nx, ny, dirtGrid[nx][ny]);
            return;
        }

        int downX = rx; int downY = ry + 1;
        if (downY < GRID_SIZE && grid[downX][downY] != 1 && !visited[downX][downY]) {
            robot.setDirection("Güney");
            robot.move(downX, downY, dirtGrid[downX][downY]);
            sweepDirection = sweepDirection.equals("Doğu") ? "Batı" : "Doğu";
            return;
        }

        int[] target = findNearestUnvisited();
        if (target != null) navigateWithBFS(target[0], target[1]);
        else {
            resumeX = -1; resumeY = -1;
            returningToStation = true;
            missionCompleted = true;
        }
    }

    private int[] findNearestUnvisited() {
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] bfsVisited = new boolean[GRID_SIZE][GRID_SIZE];
        queue.add(new int[]{robot.getX(), robot.getY()});
        bfsVisited[robot.getX()][robot.getY()] = true;
        int[] dx = {0, 0, 1, -1}; int[] dy = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            int[] curr = queue.poll(); int cx = curr[0]; int cy = curr[1];
            if (!visited[cx][cy] && grid[cx][cy] != 1) return new int[]{cx, cy};
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i]; int ny = cy + dy[i];
                if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && !bfsVisited[nx][ny] && grid[nx][ny] != 1) {
                    queue.add(new int[]{nx, ny});
                    bfsVisited[nx][ny] = true;
                }
            }
        }
        return null;
    }

    private void navigateWithBFS(int targetX, int targetY) {
        int startX = robot.getX(); int startY = robot.getY();
        if (startX == targetX && startY == targetY) return;

        Queue<int[]> queue = new LinkedList<>();
        boolean[][] bfsVisited = new boolean[GRID_SIZE][GRID_SIZE];
        int[][][] parent = new int[GRID_SIZE][GRID_SIZE][2];

        queue.add(new int[]{startX, startY});
        bfsVisited[startX][startY] = true;
        int[] dx = {0, 0, 1, -1}; int[] dy = {1, -1, 0, 0};
        boolean found = false;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll(); int cx = curr[0]; int cy = curr[1];
            if (cx == targetX && cy == targetY) { found = true; break; }
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i]; int ny = cy + dy[i];
                if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && !bfsVisited[nx][ny] && grid[nx][ny] != 1) {
                    queue.add(new int[]{nx, ny});
                    bfsVisited[nx][ny] = true;
                    parent[nx][ny] = new int[]{cx, cy};
                }
            }
        }

        if (found) {
            int currX = targetX; int currY = targetY;
            while (parent[currX][currY][0] != startX || parent[currX][currY][1] != startY) {
                int px = parent[currX][currY][0]; int py = parent[currX][currY][1];
                currX = px; currY = py;
            }
            if (currX > startX) robot.setDirection("Doğu");
            else if (currX < startX) robot.setDirection("Batı");
            else if (currY > startY) robot.setDirection("Güney");
            else if (currY < startY) robot.setDirection("Kuzey");
            robot.move(currX, currY, dirtGrid[currX][currY]);
        }
    }

    private void moveRandom() {
        int[] dx = {0, 0, 1, -1}; int[] dy = {1, -1, 0, 0};
        String[] dirs = {"Güney", "Kuzey", "Doğu", "Batı"};
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int nx = robot.getX() + dx[i]; int ny = robot.getY() + dy[i];
            if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && grid[nx][ny] != 1) validIndices.add(i);
        }
        if (!validIndices.isEmpty()) {
            int index = validIndices.get((int) (Math.random() * validIndices.size()));
            robot.setDirection(dirs[index]);
            robot.move(robot.getX() + dx[index], robot.getY() + dy[index], dirtGrid[robot.getX()][robot.getY()]);
        }
    }

    private void moveWallFollow() {
        int nx = robot.getX(); int ny = robot.getY(); String dir = robot.getDirection();
        if (dir.equals("Doğu")) nx++; else if (dir.equals("Batı")) nx--;
        else if (dir.equals("Kuzey")) ny--; else if (dir.equals("Güney")) ny++;

        if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && grid[nx][ny] != 1) robot.move(nx, ny, dirtGrid[nx][ny]);
        else {
            if (dir.equals("Doğu")) robot.setDirection("Güney");
            else if (dir.equals("Güney")) robot.setDirection("Batı");
            else if (dir.equals("Batı")) robot.setDirection("Kuzey");
            else robot.setDirection("Doğu");
        }
    }

    private void moveSpiral() { moveRandom(); }

    public int[][] getGrid() { return grid; }
    public DirtType[][] getDirtGrid() { return dirtGrid; }
    public int[][] getDirtHealth() { return dirtHealth; }
    public List<Furniture> getFurnitureList() { return furnitureList; }
    public int getRobotX() { return robot.getX(); }
    public int getRobotY() { return robot.getY(); }
    public double getRobotBattery() { return robot.getBattery(); }
    public double getRobotDustbinPercentage() { return robot.getDustbinPercentage(); }
    public double getRobotLiquidTankPercentage() { return robot.getLiquidTankPercentage(); } // YENİ
    public String getRobotDirection() { return robot.getDirection(); }
    public int getStationX() { return stationX; }
    public int getStationY() { return stationY; }
    public int getCleanedCount() { return cleanedCount; }
    public int getTotalDirtCount() { return totalDirtCount; }
    public boolean isMissionCompleted() { return missionCompleted; }
    public int getElapsedSeconds() { return elapsedSeconds; }
    public boolean isCharging() { return isCharging; }
    public boolean isOnHighPowerCarpet() { return grid[robot.getX()][robot.getY()] == 2; } // YENİ: Sadece büyük halıda uyarı verir
}