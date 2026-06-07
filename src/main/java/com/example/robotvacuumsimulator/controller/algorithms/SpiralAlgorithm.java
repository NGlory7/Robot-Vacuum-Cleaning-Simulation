package com.example.robotvacuumsimulator.controller.algorithms;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.model.Cell;
import com.example.robotvacuumsimulator.model.CellType;
import com.example.robotvacuumsimulator.model.GridMap;
import com.example.robotvacuumsimulator.model.Robot;

import java.util.ArrayList;
import java.util.List;

public class SpiralAlgorithm implements MovementAlgorithm {
    private List<int[]> plannedPath = new ArrayList<>();
    
    private int spiralStepsTaken = 0;
    private int spiralMaxSteps = 1;
    private int spiralTurns = 0;
    private int spiralDirectionIndex = 0;
    
    private final String[] spiralDirs = {"Doğu", "Güney", "Batı", "Kuzey"};
    private final int[] spiralDx = {1, 0, -1, 0};
    private final int[] spiralDy = {0, 1, 0, -1};
    
    private SmartRouteAlgorithm fallbackSmartAlgorithm;
    
    public SpiralAlgorithm() {
        this.fallbackSmartAlgorithm = new SmartRouteAlgorithm();
    }

    public void reset() {
        spiralStepsTaken = 0;
        spiralMaxSteps = 1;
        spiralTurns = 0;
        spiralDirectionIndex = 0;
        fallbackSmartAlgorithm.reset();
    }

    @Override
    public void move(Robot robot, GameController controller) {
        int rx = robot.getX(); 
        int ry = robot.getY();
        GridMap map = controller.getMap();

        plannedPath.clear();
        int simX = rx, simY = ry;
        int simSteps = spiralStepsTaken;
        int simMax = spiralMaxSteps;
        int simTurns = spiralTurns;
        int simDirIdx = spiralDirectionIndex;

        // Planlanan yolu hesapla (GUI için)
        for (int i = 0; i < 30; i++) {
            int snx = simX + spiralDx[simDirIdx];
            int sny = simY + spiralDy[simDirIdx];

            Cell simCell = map.getCell(snx, sny);
            if (simCell == null || simCell.getType() == CellType.SOLID || simCell.isVisited()) {
                break;
            }

            plannedPath.add(new int[]{snx, sny});
            simX = snx; simY = sny;
            simSteps++;

            if (simSteps == simMax) {
                simSteps = 0;
                simDirIdx = (simDirIdx + 1) % 4;
                simTurns++;
                if (simTurns % 2 == 0) simMax++;
            }
        }

        // Gerçek hareketi hesapla
        int nx = rx + spiralDx[spiralDirectionIndex];
        int ny = ry + spiralDy[spiralDirectionIndex];

        Cell nextCell = map.getCell(nx, ny);
        // Eğer spiral çizemiyorsa, akıllı rotaya (SmartRoute) devret
        if (nextCell == null || nextCell.getType() == CellType.SOLID || nextCell.isVisited()) {
            fallbackSmartAlgorithm.move(robot, controller);
            plannedPath = fallbackSmartAlgorithm.getPlannedPath(); // Yolu da ondan al
            return;
        }

        robot.setDirection(spiralDirs[spiralDirectionIndex]);
        robot.move(nx, ny, nextCell.getDirtType());
        spiralStepsTaken++;

        if (spiralStepsTaken == spiralMaxSteps) {
            spiralStepsTaken = 0;
            spiralDirectionIndex = (spiralDirectionIndex + 1) % 4;
            spiralTurns++;
            if (spiralTurns % 2 == 0) spiralMaxSteps++;
        }
    }

    @Override
    public List<int[]> getPlannedPath() {
        return plannedPath;
    }

    @Override
    public String getName() {
        return "Spiral";
    }
}
