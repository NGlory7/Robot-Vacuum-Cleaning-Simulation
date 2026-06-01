package com.example.robotvacuumsimulator.view;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.model.DirtType;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class SimulationGUI extends BorderPane {
    private final Canvas canvas;
    private final GameController controller;

    // UI Bileşenleri
    private final Label lblStatus;
    private final Label lblBattery;
    private final Label lblStats;
    private final ComboBox<String> cmbDirtType;
    private final ComboBox<String> cmbAlgorithm;
    private final Slider sldSpeed;

    private final int CELL_SIZE = 25; // Her hücre 25x25 piksel
    private final int GRID_SIZE = 16; // 16x16 Oda matrisi

    public SimulationGUI(GameController controller) {
        this.controller = controller;
        this.canvas = new Canvas(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);

        this.lblStatus = new Label("Durum: Hazır");
        this.lblBattery = new Label("Batarya: %100.0");
        this.lblStats = new Label("Temizlenen: %0 | Kalan: 0");

        this.cmbDirtType = new ComboBox<>();
        this.cmbDirtType.getItems().addAll("Toz", "Sıvı", "Leke");
        this.cmbDirtType.setValue("Toz");

        this.cmbAlgorithm = new ComboBox<>();
        this.cmbAlgorithm.getItems().addAll("Rastgele", "Spiral", "Duvar Takip");
        this.cmbAlgorithm.setValue("Rastgele");

        this.sldSpeed = new Slider(1, 5, 1);
        this.sldSpeed.setShowTickMarks(true);
        this.sldSpeed.setShowTickLabels(true);
        this.sldSpeed.setMajorTickUnit(1);
        this.sldSpeed.setBlockIncrement(1);

        setupUI();
    }

    private void setupUI() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #2F3542; -fx-text-fill: white;");
        sidebar.setPrefWidth(220);

        // Stil tanımlamaları
        String labelStyle = "-fx-text-fill: white; -fx-font-weight: bold;";
        lblStatus.setStyle(labelStyle);
        lblBattery.setStyle(labelStyle);
        lblStats.setStyle(labelStyle);

        Label title1 = new Label("Araçlar"); title1.setStyle("-fx-text-fill: #10AC84; -fx-font-size: 14px; -fx-font-weight: bold;");
        Button btnAddObstacle = new Button("Mobilya Ekle modu");
        btnAddObstacle.setMaxWidth(Double.MAX_VALUE);

        // Canvas tıklama olayı (Kir veya Engel eklemek için)
        canvas.setOnMouseClicked(e -> {
            int x = (int) (e.getX() / CELL_SIZE);
            int y = (int) (e.getY() / CELL_SIZE);

            if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
                // Eğer Shift basılıysa engel koy, değilse seçili kiri koy
                if (e.isShiftDown()) {
                    controller.addObstacle(x, y);
                } else {
                    String selected = cmbDirtType.getValue();
                    DirtType type = DirtType.DUST;
                    if (selected.equals("Sıvı")) type = DirtType.LIQUID;
                    if (selected.equals("Leke")) type = DirtType.STAIN;
                    controller.addDirt(x, y, type);
                }
                draw();
            }
        });

        Label title2 = new Label("Temizlik Algoritması"); title2.setStyle("-fx-text-fill: #10AC84; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label title3 = new Label("Robot Hızı"); title3.setStyle("-fx-text-fill: #10AC84; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Kontroller
        Button btnStart = new Button("Başlat");
        Button btnPause = new Button("Duraklat");
        Button btnReset = new Button("Sıfırla");
        Button btnToStation = new Button("İstasyona Dön");
        Button btnManualBattery = new Button("Batarya Doldur (%100)");

        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnPause.setMaxWidth(Double.MAX_VALUE);
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnToStation.setMaxWidth(Double.MAX_VALUE);
        btnManualBattery.setMaxWidth(Double.MAX_VALUE);

        btnStart.setOnAction(e -> controller.startSimulation((int) sldSpeed.getValue(), cmbAlgorithm.getValue()));
        btnPause.setOnAction(e -> controller.pauseSimulation());
        btnReset.setOnAction(e -> { controller.resetSimulation(); draw(); });
        btnToStation.setOnAction(e -> controller.sendToStation());
        btnManualBattery.setOnAction(e -> { controller.chargeManual(); draw(); });
        Label lblInfo = new Label("Tıklama: Kir Ekler\nShift+Tıklama: Engel");
        lblInfo.setStyle("-fx-text-fill: #A4B0BE; -fx-font-size: 11px;"); // Rengini gri-beyaz yaptık
        sidebar.getChildren().addAll(
                title1, new Label("Kir Türü:"), cmbDirtType, new Label("Tıklama: Kir Ekler\nShift+Tıklama: Engel"),
                title2, cmbAlgorithm,
                title3, sldSpeed,
                new Separator(),
                lblStatus, lblBattery, lblStats,
                new Separator(),
                btnStart, btnPause, btnToStation, btnManualBattery, btnReset
        );

        this.setCenter(canvas);
        this.setLeft(sidebar);
    }

    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int[][] grid = controller.getGrid();
        DirtType[][] dirtGrid = controller.getDirtGrid();
        int rx = controller.getRobotX();
        int ry = controller.getRobotY();
        int sx = controller.getStationX();
        int sy = controller.getStationY();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                // Hücre arka planı
                gc.setStroke(Color.web("#DFE4EA"));
                gc.strokeRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                if (grid[i][j] == 1) { // Engel / Mobilya
                    gc.setFill(Color.web("#747D8C"));
                    gc.fillRect(i * CELL_SIZE + 1, j * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                } else {
                    // Kirleri Çiz
                    DirtType dt = dirtGrid[i][j];
                    if (dt == DirtType.DUST) {
                        gc.setFill(Color.web("#F1F2F6"));
                        gc.fillOval(i * CELL_SIZE + 6, j * CELL_SIZE + 6, 12, 12);
                    } else if (dt == DirtType.LIQUID) {
                        gc.setFill(Color.web("#70A1FF"));
                        gc.fillRect(i * CELL_SIZE + 4, j * CELL_SIZE + 4, CELL_SIZE - 8, CELL_SIZE - 8);
                    } else if (dt == DirtType.STAIN) {
                        gc.setFill(Color.web("#FFA502"));
                        gc.fillOval(i * CELL_SIZE + 4, j * CELL_SIZE + 4, CELL_SIZE - 8, CELL_SIZE - 8);
                    }
                }

                // Şarj istasyonu çizimi
                if (i == sx && j == sy) {
                    gc.setFill(Color.web("#2ED573"));
                    gc.fillRect(i * CELL_SIZE + 2, j * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                    gc.setFill(Color.WHITE);
                    gc.fillText("⚡", i * CELL_SIZE + 6, j * CELL_SIZE + 18);
                }
            }
        }

        // Robot Çizimi
        gc.setFill(Color.web("#2F3542"));
        gc.fillOval(rx * CELL_SIZE + 2, ry * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
        gc.setFill(Color.WHITE);
        gc.fillOval(rx * CELL_SIZE + 8, ry * CELL_SIZE + 8, 8, 8); // İç halka

        // Durum Verilerini Güncelleme
        lblBattery.setText(String.format("Batarya: %%%.1f", controller.getRobotBattery()));
        lblStatus.setText("Yön: " + controller.getRobotDirection());

        int totalDirt = controller.getTotalDirtCount();
        int cleaned = controller.getCleanedCount();
        lblStats.setText("Temizlenen: " + cleaned + " | Kalan Kir: " + totalDirt);
    }
}