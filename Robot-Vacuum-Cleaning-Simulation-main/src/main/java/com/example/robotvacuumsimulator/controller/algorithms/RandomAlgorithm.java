package com.example.robotvacuumsimulator.controller.algorithms;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.model.Robot;
import com.example.robotvacuumsimulator.model.Cell;
import com.example.robotvacuumsimulator.model.CellType;
import com.example.robotvacuumsimulator.model.DirtType;
import com.example.robotvacuumsimulator.model.GridMap;

import java.util.ArrayList;
import java.util.List;

public class RandomAlgorithm implements MovementAlgorithm {
    private List<int[]> plannedPath = new ArrayList<>();

    @Override
    public void move(Robot robot, GameController controller) {
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        String[] dirs = {"Güney", "Kuzey", "Doğu", "Batı"};
        List<Integer> validIndices = new ArrayList<>();
        
        GridMap map = controller.getMap();
        
        for (int i = 0; i < 4; i++) {
            int nx = robot.getX() + dx[i]; 
            int ny = robot.getY() + dy[i];
            Cell cell = map.getCell(nx, ny);
            if (cell != null && cell.getType() != CellType.SOLID) {
                validIndices.add(i);
            }
        }
        
        if (!validIndices.isEmpty()) {
            int index = validIndices.get((int) (Math.random() * validIndices.size()));
            int nx = robot.getX() + dx[index];
            int ny = robot.getY() + dy[index];
            
            plannedPath.clear();
            plannedPath.add(new int[]{nx, ny});
            robot.setDirection(dirs[index]);
            
            Cell targetCell = map.getCell(nx, ny);
            robot.move(nx, ny, targetCell != null ? targetCell.getDirtType() : DirtType.NONE);
        }
    }

    @Override
    public List<int[]> getPlannedPath() {
        return plannedPath;
    }

    @Override
    public String getName() {
        return "Rastgele";
    }
}
