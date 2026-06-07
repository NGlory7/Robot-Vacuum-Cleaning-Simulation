package com.example.robotvacuumsimulator.controller;

import com.example.robotvacuumsimulator.controller.algorithms.*;
import com.example.robotvacuumsimulator.model.Cell;
import com.example.robotvacuumsimulator.model.CellType;
import com.example.robotvacuumsimulator.model.DirtType;
import com.example.robotvacuumsimulator.model.Furniture;
import com.example.robotvacuumsimulator.model.FurnitureType;
import com.example.robotvacuumsimulator.model.GridMap;
import com.example.robotvacuumsimulator.model.Robot;
import com.example.robotvacuumsimulator.view.SimulationGUI;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    private final int GRID_SIZE = 16;
    private final int stationX = 0;
    private final int stationY = 0;

    private final GridMap map;
    private final Robot robot;

    private List<int[]> currentPlannedPath = new ArrayList<>();

    private boolean isResuming = false;
    private int resumeX = -1;
    private int resumeY = -1;
    private boolean missionCompleted = false;
    private boolean isCharging = false;

    private MovementAlgorithm currentAlgorithmObj;
    private SmartRouteAlgorithm smartAlgo = new SmartRouteAlgorithm();
    private WallFollowAlgorithm wallFollowAlgo = new WallFollowAlgorithm();
    private SpiralAlgorithm spiralAlgo = new SpiralAlgorithm();
    private RandomAlgorithm randomAlgo = new RandomAlgorithm();

    private SimulationGUI view;
    private AnimationTimer timer;
    private boolean isRunning = false;

    private long lastUpdate = 0;
    private long lastSecondTick = 0;
    private int elapsedSeconds = 0;

    private int speedMultiplier = 1;
    private String currentAlgoName = "Akıllı Rota";
    private boolean returningToStation = false;

    public GameController() {
        this.robot = new Robot(stationX, stationY);
        this.map = new GridMap(GRID_SIZE, stationX, stationY);
        this.currentAlgorithmObj = smartAlgo;
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
        this.currentAlgoName = algo;
        
        switch (algo) {
            case "Akıllı Rota": currentAlgorithmObj = smartAlgo; break;
            case "Duvar Takip": currentAlgorithmObj = wallFollowAlgo; break;
            case "Spiral": currentAlgorithmObj = spiralAlgo; break;
            default: currentAlgorithmObj = randomAlgo; break;
        }
        
        if (missionCompleted && map.getTotalDirtCount() > 0) missionCompleted = false;

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
        elapsedSeconds = 0;

        smartAlgo.reset();
        spiralAlgo.reset();

        currentPlannedPath.clear();
        map.reset();

        addFurniture(11, 2, FurnitureType.CARPET);
        addFurniture(5, 2, FurnitureType.SOFA);
        addFurniture(12, 5, FurnitureType.TABLE);
        addFurniture(2, 8, FurnitureType.ARMCHAIR);
    }

    public boolean addFurniture(int startX, int startY, FurnitureType type) {
        return map.addFurniture(startX, startY, type, robot);
    }

    public void addDirt(int x, int y, DirtType type) {
        if (map.addDirt(x, y, type)) {
            missionCompleted = false;
        }
    }

    public void removeEntity(int x, int y) {
        map.removeEntity(x, y);
    }

    public void sendToStation() {
        if (!returningToStation && !isResuming && !isCharging) {
            resumeX = robot.getX();
            resumeY = robot.getY();
        }
        returningToStation = true;
        isResuming = false;
        startSimulation(speedMultiplier, currentAlgoName);
    }

    public void setRobotBattery(double level) {
        robot.setBattery(level);

        if (returningToStation && level > 20.0 && !missionCompleted) {
            returningToStation = false;
            if (resumeX != -1 && resumeY != -1 && (resumeX != stationX || resumeY != stationY)) {
                isResuming = true;
            }
        }
        if (view != null) view.draw();
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

        if (!missionCompleted && map.getTotalDirtCount() == 0) {
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
        Cell currentCell = map.getCell(rx, ry);
        
        if (currentCell != null) {
            currentCell.setVisited(true);
            
            if (currentCell.getType() == CellType.CARPET) {
                robot.setBattery(robot.getBattery() - 1.2);
            }

            DirtType currentDirt = currentCell.getDirtType();
            if (currentDirt != DirtType.NONE) {
                currentCell.decreaseDirtHealth();

                if (currentDirt == DirtType.LIQUID) robot.addLiquid(1);
                else robot.addDust(1);

                if (currentCell.getDirtHealth() <= 0) {
                    currentCell.setDirtType(DirtType.NONE);
                    map.incrementCleanedCount();
                    map.decrementTotalDirtCount();
                } else {
                    view.draw(); 
                    return;
                }
            }
        }

        // Strateji deseni ile algoritmanın çağrılması
        currentAlgorithmObj.move(robot, this);
        currentPlannedPath = currentAlgorithmObj.getPlannedPath();
        
        view.draw();
    }

    public void navigateWithBFS(int targetX, int targetY) {
        List<int[]> path = PathFinder.navigateWithBFS(map, robot, targetX, targetY);
        if (!path.isEmpty()) {
            currentPlannedPath = new ArrayList<>(path);
            int startX = robot.getX();
            int startY = robot.getY();
            int nextX = path.get(0)[0];
            int nextY = path.get(0)[1];
            if (nextX > startX) robot.setDirection("Doğu");
            else if (nextX < startX) robot.setDirection("Batı");
            else if (nextY > startY) robot.setDirection("Güney");
            else if (nextY < startY) robot.setDirection("Kuzey");
            
            Cell nextCell = map.getCell(nextX, nextY);
            robot.move(nextX, nextY, nextCell != null ? nextCell.getDirtType() : DirtType.NONE);
        } else {
            currentPlannedPath.clear();
        }
    }

    public GridMap getMap() { return map; }
    public void setMissionCompleted(boolean state) { this.missionCompleted = state; }
    public void setReturningToStation(boolean state) { this.returningToStation = state; }
    public void clearResumePosition() { this.resumeX = -1; this.resumeY = -1; }
    public List<int[]> getCurrentPlannedPath() { return currentPlannedPath; }
    
    // Yalnızca algoritmalar tarafından ihtiyaç duyulan kısmi map getter'ı (uyumluluk veya kolaylık için, ama getMap() tercih edilir)
    public int getGridSize() { return map.getSize(); }

    public List<Furniture> getFurnitureList() { return map.getFurnitureList(); }
    public int getRobotX() { return robot.getX(); }
    public int getRobotY() { return robot.getY(); }
    public double getRobotBattery() { return robot.getBattery(); }
    public double getRobotDustbinPercentage() { return robot.getDustbinPercentage(); }
    public double getRobotLiquidTankPercentage() { return robot.getLiquidTankPercentage(); }
    public String getRobotDirection() { return robot.getDirection(); }
    public int getStationX() { return stationX; }
    public int getStationY() { return stationY; }
    public int getCleanedCount() { return map.getCleanedCount(); }
    public int getTotalDirtCount() { return map.getTotalDirtCount(); }
    public boolean isMissionCompleted() { return missionCompleted; }
    public int getElapsedSeconds() { return elapsedSeconds; }
    public boolean isCharging() { return isCharging; }
    public boolean isOnCarpet() { 
        Cell c = map.getCell(robot.getX(), robot.getY());
        return c != null && c.getType() == CellType.CARPET; 
    }
}
