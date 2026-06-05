package com.example.robotvacuumsimulator;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.view.SimulationGUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameController controller = new GameController();
        SimulationGUI gui = new SimulationGUI(controller);
        controller.setView(gui);

        // Genişliği 850, yüksekliği 650 yaparak butonlara yer açtık
        // Genişliği 900, yüksekliği 650 yapıyoruz
        Scene scene = new Scene(gui, 900, 750);
        primaryStage.setTitle("Robot Süpürge Simülasyonu - BZ 214");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true); // İstersen köşesinden tutup büyütebilmen için true yaptık
        primaryStage.show();

        gui.draw();
    }

    public static void main(String[] args) {
        Application.launch(Launcher.class, args);
    }
}