package com.example.robotvacuumsimulator.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ArmchairRenderer implements FurnitureRenderer {
    @Override
    public void render(GraphicsContext gc, double px, double py, double pw, double ph, Image image, int cellSize) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, px, py, pw, ph);
        } else {
            gc.setFill(Color.web("#D4A373")); 
            gc.fillRoundRect(px + 4, py + 4, pw - 8, ph - 8, 15, 15);
            gc.setFill(Color.web("#FAEDCD")); 
            gc.fillRoundRect(px + 12, py + 12, pw - 24, ph - 24, 10, 10);
        }
    }
}
