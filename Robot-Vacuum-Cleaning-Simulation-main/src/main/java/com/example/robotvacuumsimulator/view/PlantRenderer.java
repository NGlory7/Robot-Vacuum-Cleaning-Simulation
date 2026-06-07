package com.example.robotvacuumsimulator.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PlantRenderer implements FurnitureRenderer {
    @Override
    public void render(GraphicsContext gc, double px, double py, double pw, double ph, Image image, int cellSize) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, px, py, pw, ph);
        } else {
            gc.setFill(Color.web("#8B4513")); 
            gc.fillOval(px + 8, py + 8, 24, 24);
            gc.setFill(Color.web("#15803D")); 
            gc.fillOval(px + 2, py + 2, 36, 36);
        }
    }
}
