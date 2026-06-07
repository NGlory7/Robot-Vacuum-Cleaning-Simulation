package com.example.robotvacuumsimulator.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class TableRenderer implements FurnitureRenderer {
    @Override
    public void render(GraphicsContext gc, double px, double py, double pw, double ph, Image image, int cellSize) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, px, py, pw, ph);
        } else {
            gc.setFill(Color.web("#8B5A2B")); 
            gc.fillRoundRect(px + 4, py + 4, pw - 8, ph - 8, 12, 12);
            gc.setFill(Color.web("#F8FAFC")); 
            gc.fillOval(px + pw/2 - 18, py + ph/2 - 18, 36, 36);
        }
    }
}
