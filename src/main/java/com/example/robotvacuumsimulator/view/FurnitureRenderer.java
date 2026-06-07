package com.example.robotvacuumsimulator.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public interface FurnitureRenderer {
    void render(GraphicsContext gc, double px, double py, double pw, double ph, Image image, int cellSize);
}
