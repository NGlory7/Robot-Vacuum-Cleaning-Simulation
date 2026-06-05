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
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private DirtType[][] dirtGrid = new DirtType[GRID_SIZE][GRID_SIZE];
    private int[][] dirtHealth = new int[GRID_SIZE][GRID_SIZE];
    private boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];

    private List<int[]> currentPlannedPath = new ArrayList<>();

    private String sweepDirection = "Doğu";
    private boolean isResuming = false;
    private int resumeX = -1;
    private int resumeY = -1;
    private boolean missionCompleted = false;
    private boolean isCharging = false;

    private int spiralStepsTaken = 0;
    private int spiralMaxSteps = 1;
    private int spiralTurns = 0;
    private int spiralDirectionIndex = 0;
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
        robot.emptyLiquidTank();
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

        spiralStepsTaken = 0;
        spiralMaxSteps = 1;
        spiralTurns = 0;
        spiralDirectionIndex = 0;

        currentPlannedPath.clear();
        furnitureList.clear();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
                dirtGrid[i][j] = DirtType.NONE;
                dirtHealth[i][j] = 0;
                visited[i][j] = false;
            }
        }

        addFurniture(11, 2, FurnitureType.CARPET);
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
                if (grid[i][j] != 0 || (i == robot.getX() && j == robot.getY()) || (i == stationX && j == stationY)) {
                    return false;
                }
            }
        }

        for (int i = startX; i < startX + w; i++) {
            for (int j = startY; j < startY + h; j++) {
                if (type == FurnitureType.CARPET) {
                    grid[i][j] = 2;
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

        // ZORUNLU GEREKSİNİM: Kir tipine göre temizlenme süresi (Health) ayarı
        if (type == DirtType.STAIN) dirtHealth[x][y] = 4; // Leke 4 adımda temizlenir
        else if (type == DirtType.LIQUID) dirtHealth[x][y] = 2; // Sıvı 2 adımda temizlenir
        else dirtHealth[x][y] = 1; // Toz 1 adımda temizlenir

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

    // ZORUNLU GEREKSİNİM: Arayüzden bataryaya manuel müdahale edebilmek için
    public void setRobotBattery(double level) {
        robot.setBattery(level);

        // Eğer şarja dönüyorduysa ama bataryayı manuel olarak doldurduysak kaldığı yere geri dönsün
        if (returningToStation && level > 20.0 && !missionCompleted) {
            returningToStation = false;
            if (resumeX != -1 && resumeY != -1 && (resumeX != stationX || resumeY != stationY)) {
                isResuming = true;
            }
        }
        if (view != null) view.draw();
    }

    private void updateSimulation() {
        if (isCharging) {
            currentPlannedPath.clear();
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
                robot.emptyLiquidTank();
                returningToStation = false;
                isCharging = true;
                currentPlannedPath.clear();
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
                currentPlannedPath.clear();
            }
            view.draw();
            return;
        }

        int rx = robot.getX();
        int ry = robot.getY();
        visited[rx][ry] = true;

        if (grid[rx][ry] == 2) robot.setBattery(robot.getBattery() - 1.2);

        DirtType currentDirt = dirtGrid[rx][ry];
        if (currentDirt != DirtType.NONE) {
            // Zamana yayılan temizlik mantığı
            dirtHealth[rx][ry]--;

            if (currentDirt == DirtType.LIQUID) robot.addLiquid(1);
            else robot.addDust(1);

            if (dirtHealth[rx][ry] <= 0) {
                dirtGrid[rx][ry] = DirtType.NONE;
                cleanedCount++;
                if (totalDirtCount > 0) totalDirtCount--;
            } else {
                view.draw(); // Kir bitmediyse hareket etmeden üzerinde bekler ve temizlemeye devam eder
                return;
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
        int rx = robot.getX(); int ry = robot.getY();

        currentPlannedPath.clear();
        int simX = rx, simY = ry;
        String simDir = sweepDirection;
        for(int i = 0; i < 15; i++) {
            int snx = simX, sny = simY;
            if (simDir.equals("Doğu")) snx++; else if (simDir.equals("Batı")) snx--;

            if (snx >= 0 && snx < GRID_SIZE && sny >= 0 && sny < GRID_SIZE && grid[snx][sny] != 1 && !visited[snx][sny]) {
                currentPlannedPath.add(new int[]{snx, sny});
                simX = snx; simY = sny;
            } else {
                int downX = simX; int downY = simY + 1;
                if (downY < GRID_SIZE && grid[downX][downY] != 1 && !visited[downX][downY]) {
                    currentPlannedPath.add(new int[]{downX, downY});
                    simX = downX; simY = downY;
                    simDir = simDir.equals("Doğu") ? "Batı" : "Doğu";
                } else {
                    break;
                }
            }
        }

        int nx = rx, ny = ry;
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
        if (startX == targetX && startY == targetY) {
            currentPlannedPath.clear();
            return;
        }

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
            List<int[]> path = new ArrayList<>();
            int currX = targetX; int currY = targetY;
            while (currX != startX || currY != startY) {
                path.add(0, new int[]{currX, currY});
                int px = parent[currX][currY][0];
                int py = parent[currX][currY][1];
                currX = px; currY = py;
            }
            currentPlannedPath = new ArrayList<>(path);

            int nextX = path.get(0)[0];
            int nextY = path.get(0)[1];
            if (nextX > startX) robot.setDirection("Doğu");
            else if (nextX < startX) robot.setDirection("Batı");
            else if (nextY > startY) robot.setDirection("Güney");
            else if (nextY < startY) robot.setDirection("Kuzey");
            robot.move(nextX, nextY, dirtGrid[nextX][nextY]);
        } else {
            currentPlannedPath.clear();
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
            currentPlannedPath.clear();
            currentPlannedPath.add(new int[]{robot.getX() + dx[index], robot.getY() + dy[index]});
            robot.setDirection(dirs[index]);
            robot.move(robot.getX() + dx[index], robot.getY() + dy[index], dirtGrid[robot.getX()][robot.getY()]);
        }
    }

    private void moveWallFollow() {
        int rx = robot.getX(); int ry = robot.getY(); String dir = robot.getDirection();

        currentPlannedPath.clear();
        int simX = rx, simY = ry;
        String simDir = dir;
        for(int i = 0; i < 15; i++) {
            int snx = simX; int sny = simY;
            if (simDir.equals("Doğu")) snx++; else if (simDir.equals("Batı")) snx--;
            else if (simDir.equals("Kuzey")) sny--; else if (simDir.equals("Güney")) sny++;

            if (snx >= 0 && snx < GRID_SIZE && sny >= 0 && sny < GRID_SIZE && grid[snx][sny] != 1) {
                currentPlannedPath.add(new int[]{snx, sny});
                simX = snx; simY = sny;
            } else {
                if (simDir.equals("Doğu")) simDir = "Güney";
                else if (simDir.equals("Güney")) simDir = "Batı";
                else if (simDir.equals("Batı")) simDir = "Kuzey";
                else simDir = "Doğu";
            }
        }

        int nx = rx; int ny = ry;
        if (dir.equals("Doğu")) nx++; else if (dir.equals("Batı")) nx--;
        else if (dir.equals("Kuzey")) ny--; else if (dir.equals("Güney")) ny++;

        if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE && grid[nx][ny] != 1) {
            robot.move(nx, ny, dirtGrid[nx][ny]);
        }
        else {
            if (dir.equals("Doğu")) robot.setDirection("Güney");
            else if (dir.equals("Güney")) robot.setDirection("Batı");
            else if (dir.equals("Batı")) robot.setDirection("Kuzey");
            else robot.setDirection("Doğu");
        }
    }

    private void moveSpiral() {
        int rx = robot.getX(); int ry = robot.getY();

        currentPlannedPath.clear();
        int simX = rx, simY = ry;
        int simSteps = spiralStepsTaken;
        int simMax = spiralMaxSteps;
        int simTurns = spiralTurns;
        int simDirIdx = spiralDirectionIndex;

        for (int i = 0; i < 30; i++) {
            int snx = simX + spiralDx[simDirIdx];
            int sny = simY + spiralDy[simDirIdx];

            if (snx < 0 || snx >= GRID_SIZE || sny < 0 || sny >= GRID_SIZE || grid[snx][sny] == 1 || visited[snx][sny]) {
                break;
            }

            currentPlannedPath.add(new int[]{snx, sny});
            simX = snx; simY = sny;
            simSteps++;

            if (simSteps == simMax) {
                simSteps = 0;
                simDirIdx = (simDirIdx + 1) % 4;
                simTurns++;
                if (simTurns % 2 == 0) simMax++;
            }
        }

        int nx = rx + spiralDx[spiralDirectionIndex];
        int ny = ry + spiralDy[spiralDirectionIndex];

        if (nx < 0 || nx >= GRID_SIZE || ny < 0 || ny >= GRID_SIZE || grid[nx][ny] == 1 || visited[nx][ny]) {
            moveSmart();
            return;
        }

        robot.setDirection(spiralDirs[spiralDirectionIndex]);
        robot.move(nx, ny, dirtGrid[nx][ny]);
        spiralStepsTaken++;

        if (spiralStepsTaken == spiralMaxSteps) {
            spiralStepsTaken = 0;
            spiralDirectionIndex = (spiralDirectionIndex + 1) % 4;
            spiralTurns++;
            if (spiralTurns % 2 == 0) spiralMaxSteps++;
        }
    }

    public int[][] getGrid() { return grid; }
    public DirtType[][] getDirtGrid() { return dirtGrid; }
    public int[][] getDirtHealth() { return dirtHealth; }
    public List<int[]> getCurrentPlannedPath() { return currentPlannedPath; }
    public List<Furniture> getFurnitureList() { return furnitureList; }
    public int getRobotX() { return robot.getX(); }
    public int getRobotY() { return robot.getY(); }
    public double getRobotBattery() { return robot.getBattery(); }
    public double getRobotDustbinPercentage() { return robot.getDustbinPercentage(); }
    public double getRobotLiquidTankPercentage() { return robot.getLiquidTankPercentage(); }
    public String getRobotDirection() { return robot.getDirection(); }
    public int getStationX() { return stationX; }
    public int getStationY() { return stationY; }
    public int getCleanedCount() { return cleanedCount; }
    public int getTotalDirtCount() { return totalDirtCount; }
    public boolean isMissionCompleted() { return missionCompleted; }
    public int getElapsedSeconds() { return elapsedSeconds; }
    public boolean isCharging() { return isCharging; }
    public boolean isOnCarpet() { return grid[robot.getX()][robot.getY()] == 2; }
}