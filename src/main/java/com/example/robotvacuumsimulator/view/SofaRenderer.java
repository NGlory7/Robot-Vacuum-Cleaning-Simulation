package com.example.robotvacuumsimulator.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class SofaRenderer implements FurnitureRenderer {
    @Override
    public void render(GraphicsContext gc, double px, double py, double pw, double ph, Image image, int cellSize) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, px, py, pw, ph);
        } else {
            gc.setFill(Color.web("#CCD5AE")); 
            gc.fillRoundRect(px + 4, py + 4, pw - 8, ph - 8, 15, 15);
            gc.setFill(Color.web("#E9EDC9")); 
            gc.fillRoundRect(px + 8, py + 8, 22, ph - 16, 8, 8);
            gc.fillRoundRect(px + pw - 30, py + 8, 22, ph - 16, 8, 8);
        }
    }
}
