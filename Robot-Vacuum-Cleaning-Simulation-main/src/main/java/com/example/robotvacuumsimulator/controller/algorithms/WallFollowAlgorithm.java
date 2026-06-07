package com.example.robotvacuumsimulator.controller.algorithms;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.model.Cell;
import com.example.robotvacuumsimulator.model.CellType;
import com.example.robotvacuumsimulator.model.DirtType;
import com.example.robotvacuumsimulator.model.GridMap;
import com.example.robotvacuumsimulator.model.Robot;

import java.util.ArrayList;
import java.util.List;

public class WallFollowAlgorithm implements MovementAlgorithm {
    private List<int[]> plannedPath = new ArrayList<>();

    @Override
    public void move(Robot robot, GameController controller) {
        int rx = robot.getX(); 
        int ry = robot.getY(); 
        String dir = robot.getDirection();
        
        GridMap map = controller.getMap();

        plannedPath.clear();
        int simX = rx, simY = ry;
        String simDir = dir;
        
        // Planlanan yolu hesapla (Simülasyon - GUI için)
        for(int i = 0; i < 15; i++) {
            int snx = simX; int sny = simY;
            if (simDir.equals("Doğu")) snx++; 
            else if (simDir.equals("Batı")) snx--;
            else if (simDir.equals("Kuzey")) sny--; 
            else if (simDir.equals("Güney")) sny++;

            Cell simCell = map.getCell(snx, sny);
            if (simCell != null && simCell.getType() != CellType.SOLID) {
                plannedPath.add(new int[]{snx, sny});
                simX = snx; simY = sny;
            } else {
                if (simDir.equals("Doğu")) simDir = "Güney";
                else if (simDir.equals("Güney")) simDir = "Batı";
                else if (simDir.equals("Batı")) simDir = "Kuzey";
                else simDir = "Doğu";
            }
        }

        // Gerçek hareketi yap
        int nx = rx; int ny = ry;
        if (dir.equals("Doğu")) nx++; 
        else if (dir.equals("Batı")) nx--;
        else if (dir.equals("Kuzey")) ny--; 
        else if (dir.equals("Güney")) ny++;

        Cell nextCell = map.getCell(nx, ny);
        if (nextCell != null && nextCell.getType() != CellType.SOLID) {
            robot.move(nx, ny, nextCell.getDirtType());
        }
        else {
            // Engele çarptıysa dön
            if (dir.equals("Doğu")) robot.setDirection("Güney");
            else if (dir.equals("Güney")) robot.setDirection("Batı");
            else if (dir.equals("Batı")) robot.setDirection("Kuzey");
            else robot.setDirection("Doğu");
        }
    }

    @Override
    public List<int[]> getPlannedPath() {
        return plannedPath;
    }

    @Override
    public String getName() {
        return "Duvar Takip";
    }
}
