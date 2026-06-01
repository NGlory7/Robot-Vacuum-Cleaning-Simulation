package com.example.robotvacuumsimulator.controller;

import com.example.robotvacuumsimulator.model.Robot;
import com.example.robotvacuumsimulator.model.DirtType;
import com.example.robotvacuumsimulator.view.SimulationGUI;
import javafx.animation.AnimationTimer;
import java.util.*;

public class GameController {
    private final int GRID_SIZE = 16;
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private DirtType[][] dirtGrid = new DirtType[GRID_SIZE][GRID_SIZE];
    private int[][] dirtHealth = new int[GRID_SIZE][GRID_SIZE];

    private boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
    private String sweepDirection = "Doğu";
    private boolean isResuming = false;
    private int resumeX = -1;
    private int resumeY = -1;

    private boolean missionCompleted = false;

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
        if (missionCompleted && totalDirtCount > 0) {
            missionCompleted = false;
        }
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
        robot.setPosition(stationX, stationY);
        robot.setBattery(100.0);
        robot.emptyDustbin();
        returningToStation = false;
        isResuming = false;
        missionCompleted = false;
        resumeX = -1;
        resumeY = -1;
        sweepDirection = "Doğu";
        cleanedCount = 0;
        totalDirtCount = 0;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
                dirtGrid[i][j] = DirtType.NONE;
                dirtHealth[i][j] = 0;
                visited[i][j] = false;
            }
        }

        grid[5][5] = 1; grid[5][6] = 1; grid[6][5] = 1;
        grid[10][12] = 1; grid[11][12] = 1;
    }

    public void addObstacle(int x, int y) {
        if ((x == robot.getX() && y == robot.getY()) || (x == stationX && y == stationY)) return;
        grid[x][y] = grid[x][y] == 1 ? 0 : 1;
        dirtGrid[x][y] = DirtType.NONE;
        dirtHealth[x][y] = 0;
    }

    public void addDirt(int x, int y, DirtType type) {
        if (grid[x][y] == 1) return;
        if (dirtGrid[x][y] == DirtType.NONE) totalDirtCount++;
        dirtGrid[x][y] = type;

        // YENİ: Leke eklendiğinde 3, 4 veya 5 puanlık rastgele bir zorluk derecesi atanır
        if (type == DirtType.STAIN) {
            dirtHealth[x][y] = 3 + (int)(Math.random() * 3);
        } else {
            dirtHealth[x][y] = 1;
        }

        visited[x][y] = false;
        missionCompleted = false;
    }

    public void sendToStation() {
        if (!returningToStation && !isResuming) {
            resumeX = robot.getX();
            resumeY = robot.getY();
        }
        returningToStation = true;
        isResuming = false;
        startSimulation(speedMultiplier, currentAlgo);
    }

    private void updateSimulation() {
        if (!missionCompleted && totalDirtCount == 0) {
            missionCompleted = true;
            returningToStation = true;
            isResuming = false;
            resumeX = -1;
            resumeY = -1;
        }

        if (!returningToStation && !isResuming && !missionCompleted && (robot.getBattery() <= 20.0 || robot.isDustbinFull())) {
            resumeX = robot.getX();
            resumeY = robot.getY();
            returningToStation = true;
        }

        if (returningToStation) {
            navigateWithBFS(stationX, stationY);
            if (robot.getX() == stationX && robot.getY() == stationY) {
                robot.setBattery(100.0);
                robot.emptyDustbin();
                returningToStation = false;

                if (missionCompleted) {
                    pauseSimulation();
                } else if (resumeX != -1 && resumeY != -1 && (resumeX != stationX || resumeY != stationY)) {
                    isResuming = true;
                } else {
                    pauseSimulation();
                }
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

        // --- YENİ TEMİZLİK MANTIĞI ---
        int rx = robot.getX();
        int ry = robot.getY();
        visited[rx][ry] = true;

        DirtType currentDirt = dirtGrid[rx][ry];
        if (currentDirt != DirtType.NONE) {

            if (currentDirt == DirtType.STAIN) {
                dirtHealth[rx][ry]--; // Fırçalama: Lekenin canını 1 düşür
                robot.addDust(1);     // Hazneye 1 puan kir at

                if (dirtHealth[rx][ry] <= 0) {
                    // Leke tamamen silindi
                    dirtGrid[rx][ry] = DirtType.NONE;
                    cleanedCount++;
                    if (totalDirtCount > 0) totalDirtCount--;
                } else {
                    // YENİ: Leke henüz bitmedi. Robot olduğu yerde bekleyecek (fırçalamaya devam edecek)
                    view.draw();
                    return; // Return ile fonksiyonu burada kesiyoruz ki aşağıdaki move() algoritmaları çalışmasın!
                }
            } else {
                // Sıvı veya Toz ise tek seferde çekilir
                int dustAmount = (currentDirt == DirtType.LIQUID) ? 2 : 1;
                robot.addDust(dustAmount);
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
        int rx = robot.getX();
        int ry = robot.getY();

        int nx = rx, ny = ry;
        if (sweepDirection.equals("Doğu")) nx++;
        else if (sweepDirection.equals("Batı")) nx--;

        if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && grid[nx][ny] == 0 && !visited[nx][ny]) {
            robot.setDirection(sweepDirection);
            robot.move(nx, ny, dirtGrid[nx][ny]);
            return;
        }

        int downX = rx;
        int downY = ry + 1;

        if (downY < GRID_SIZE && grid[downX][downY] == 0 && !visited[downX][downY]) {
            robot.setDirection("Güney");
            robot.move(downX, downY, dirtGrid[downX][downY]);
            sweepDirection = sweepDirection.equals("Doğu") ? "Batı" : "Doğu";
            return;
        }

        int[] target = findNearestUnvisited();
        if (target != null) {
            navigateWithBFS(target[0], target[1]);
        } else {
            resumeX = -1;
            resumeY = -1;
            returningToStation = true;
            missionCompleted = true;
        }
    }

    private int[] findNearestUnvisited() {
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] bfsVisited = new boolean[GRID_SIZE][GRID_SIZE];

        queue.add(new int[]{robot.getX(), robot.getY()});
        bfsVisited[robot.getX()][robot.getY()] = true;

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0];
            int cy = curr[1];

            if (!visited[cx][cy] && grid[cx][cy] == 0) {
                return new int[]{cx, cy};
            }

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];

                if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
                    if (!bfsVisited[nx][ny] && grid[nx][ny] == 0) {
                        queue.add(new int[]{nx, ny});
                        bfsVisited[nx][ny] = true;
                    }
                }
            }
        }
        return null;
    }

    private void navigateWithBFS(int targetX, int targetY) {
        int startX = robot.getX();
        int startY = robot.getY();
        if (startX == targetX && startY == targetY) return;

        Queue<int[]> queue = new LinkedList<>();
        boolean[][] bfsVisited = new boolean[GRID_SIZE][GRID_SIZE];
        int[][][] parent = new int[GRID_SIZE][GRID_SIZE][2];

        queue.add(new int[]{startX, startY});
        bfsVisited[startX][startY] = true;

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
                    if (!bfsVisited[nx][ny] && grid[nx][ny] == 0) {
                        queue.add(new int[]{nx, ny});
                        bfsVisited[nx][ny] = true;
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

            if (currX > startX) robot.setDirection("Doğu");
            else if (currX < startX) robot.setDirection("Batı");
            else if (currY > startY) robot.setDirection("Güney");
            else if (currY < startY) robot.setDirection("Kuzey");

            robot.move(currX, currY, dirtGrid[currX][currY]);
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
    public int getRobotX() { return robot.getX(); }
    public int getRobotY() { return robot.getY(); }
    public double getRobotBattery() { return robot.getBattery(); }
    public double getRobotDustbinPercentage() { return robot.getDustbinPercentage(); }
    public String getRobotDirection() { return robot.getDirection(); }
    public int getStationX() { return stationX; }
    public int getStationY() { return stationY; }
    public int getCleanedCount() { return cleanedCount; }
    public int getTotalDirtCount() { return totalDirtCount; }
    public boolean isMissionCompleted() { return missionCompleted; }
}