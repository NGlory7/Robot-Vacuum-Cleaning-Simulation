package com.example.robotvacuumsimulator.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class TvUnitRenderer implements FurnitureRenderer {
    @Override
    public void render(GraphicsContext gc, double px, double py, double pw, double ph, Image image, int cellSize) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, px, py, pw, ph);
        } else {
            gc.setFill(Color.web("#334155")); 
            gc.fillRoundRect(px + 4, py + 4, pw - 8, ph - 8, 6, 6);
            gc.setFill(Color.web("#0F172A")); 
            gc.fillRect(px + 8, py + cellSize - 10, pw - 16, cellSize * 2 + 20);
        }
    }
}
