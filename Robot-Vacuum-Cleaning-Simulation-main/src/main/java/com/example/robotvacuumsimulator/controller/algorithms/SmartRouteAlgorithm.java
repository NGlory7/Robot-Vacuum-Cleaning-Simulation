package com.example.robotvacuumsimulator.controller.algorithms;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.model.Cell;
import com.example.robotvacuumsimulator.model.CellType;
import com.example.robotvacuumsimulator.model.DirtType;
import com.example.robotvacuumsimulator.model.GridMap;
import com.example.robotvacuumsimulator.model.Robot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SmartRouteAlgorithm implements MovementAlgorithm {
    private List<int[]> plannedPath = new ArrayList<>();
    private String sweepDirection = "Doğu";

    public void reset() {
        sweepDirection = "Doğu";
    }

    @Override
    public void move(Robot robot, GameController controller) {
        int rx = robot.getX(); 
        int ry = robot.getY();
        
        GridMap map = controller.getMap();

        plannedPath.clear();
        int simX = rx, simY = ry;
        String simDir = sweepDirection;
        
        // İleri planlama simülasyonu
        for(int i = 0; i < 15; i++) {
            int snx = simX, sny = simY;
            if (simDir.equals("Doğu")) snx++; 
            else if (simDir.equals("Batı")) snx--;

            Cell simCell = map.getCell(snx, sny);
            if (simCell != null && simCell.getType() != CellType.SOLID && !simCell.isVisited()) {
                plannedPath.add(new int[]{snx, sny});
                simX = snx; simY = sny;
            } else {
                int downX = simX; int downY = simY + 1;
                Cell downCell = map.getCell(downX, downY);
                if (downCell != null && downCell.getType() != CellType.SOLID && !downCell.isVisited()) {
                    plannedPath.add(new int[]{downX, downY});
                    simX = downX; simY = downY;
                    simDir = simDir.equals("Doğu") ? "Batı" : "Doğu";
                } else {
                    break;
                }
            }
        }

        // Gerçek hareket hesaplaması
        int nx = rx, ny = ry;
        if (sweepDirection.equals("Doğu")) nx++; 
        else if (sweepDirection.equals("Batı")) nx--;

        // 1. Durum: İleri gidebilir mi?
        Cell nextCell = map.getCell(nx, ny);
        if (nextCell != null && nextCell.getType() != CellType.SOLID && !nextCell.isVisited()) {
            robot.setDirection(sweepDirection);
            robot.move(nx, ny, nextCell.getDirtType());
            return;
        }

        // 2. Durum: Aşağı inebilir mi?
        int downX = rx; int downY = ry + 1;
        Cell downCell = map.getCell(downX, downY);
        if (downCell != null && downCell.getType() != CellType.SOLID && !downCell.isVisited()) {
            robot.setDirection("Güney");
            robot.move(downX, downY, downCell.getDirtType());
            sweepDirection = sweepDirection.equals("Doğu") ? "Batı" : "Doğu";
            return;
        }

        // 3. Durum: İleri gidemiyor, aşağı inemiyor -> Ziyaret edilmemiş en yakın yere git (BFS)
        int[] target = findNearestUnvisited(robot, map);
        if (target != null) {
            controller.navigateWithBFS(target[0], target[1]);
            plannedPath = controller.getCurrentPlannedPath();
        }
        else {
            // Gidecek yer kalmadıysa görevi tamamla
            controller.setMissionCompleted(true);
            controller.setReturningToStation(true);
            controller.clearResumePosition();
        }
    }

    private int[] findNearestUnvisited(Robot robot, GridMap map) {
        Queue<int[]> queue = new LinkedList<>();
        int gridSize = map.getSize();
        boolean[][] bfsVisited = new boolean[gridSize][gridSize];
        queue.add(new int[]{robot.getX(), robot.getY()});
        bfsVisited[robot.getX()][robot.getY()] = true;
        
        int[] dx = {0, 0, 1, -1}; 
        int[] dy = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            int[] curr = queue.poll(); 
            int cx = curr[0]; 
            int cy = curr[1];
            
            Cell currCell = map.getCell(cx, cy);
            if (currCell != null && !currCell.isVisited() && currCell.getType() != CellType.SOLID) {
                return new int[]{cx, cy};
            }
            
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i]; int ny = cy + dy[i];
                if (nx >= 0 && nx < gridSize && ny >= 0 && ny < gridSize && !bfsVisited[nx][ny]) {
                    Cell nCell = map.getCell(nx, ny);
                    if (nCell != null && nCell.getType() != CellType.SOLID) {
                        queue.add(new int[]{nx, ny});
                        bfsVisited[nx][ny] = true;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<int[]> getPlannedPath() {
        return plannedPath;
    }

    @Override
    public String getName() {
        return "Akıllı Rota";
    }
}
