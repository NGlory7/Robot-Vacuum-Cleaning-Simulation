package com.example.robotvacuumsimulator.controller;

import com.example.robotvacuumsimulator.model.Cell;
import com.example.robotvacuumsimulator.model.CellType;
import com.example.robotvacuumsimulator.model.GridMap;
import com.example.robotvacuumsimulator.model.Robot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PathFinder {
    
    public static List<int[]> navigateWithBFS(GridMap map, Robot robot, int targetX, int targetY) {
        int startX = robot.getX(); 
        int startY = robot.getY();
        if (startX == targetX && startY == targetY) {
            return new ArrayList<>();
        }

        int gridSize = map.getSize();
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] bfsVisited = new boolean[gridSize][gridSize];
        int[][][] parent = new int[gridSize][gridSize][2];

        queue.add(new int[]{startX, startY});
        bfsVisited[startX][startY] = true;
        int[] dx = {0, 0, 1, -1}; 
        int[] dy = {1, -1, 0, 0};
        boolean found = false;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll(); 
            int cx = curr[0]; 
            int cy = curr[1];
            if (cx == targetX && cy == targetY) { found = true; break; }
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i]; 
                int ny = cy + dy[i];
                if (nx >= 0 && nx < gridSize && ny >= 0 && ny < gridSize && !bfsVisited[nx][ny]) {
                    Cell cell = map.getCell(nx, ny);
                    if (cell != null && cell.getType() != CellType.SOLID) {
                        queue.add(new int[]{nx, ny});
                        bfsVisited[nx][ny] = true;
                        parent[nx][ny] = new int[]{cx, cy};
                    }
                }
            }
        }

        List<int[]> path = new ArrayList<>();
        if (found) {
            int currX = targetX; int currY = targetY;
            while (currX != startX || currY != startY) {
                path.add(0, new int[]{currX, currY});
                int px = parent[currX][currY][0];
                int py = parent[currX][currY][1];
                currX = px; currY = py;
            }
        }
        return path;
    }
}
