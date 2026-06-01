package com.example.robotvacuumsimulator.view;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.model.DirtType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SimulationGUI extends BorderPane {
    private final Canvas canvas;
    private final GameController controller;

    private final Label lblStatus;
    private final Label lblBattery;
    private final Label lblDustbin;
    private final Label lblStats;
    private final ComboBox<String> cmbDirtType;
    private final ComboBox<String> cmbAlgorithm;
    private final Slider sldSpeed;

    private final int CELL_SIZE = 35;
    private final int GRID_SIZE = 16;

    private final String BG_COLOR = "#121A2F";
    private final String PANEL_COLOR = "#1E293B";
    private final String TEXT_COLOR = "#E2E8F0";
    private final String ACCENT_BLUE = "#3B82F6";
    private final String ACCENT_GREEN = "#10B981";
    private final String ACCENT_RED = "#EF4444";

    public SimulationGUI(GameController controller) {
        this.controller = controller;
        this.canvas = new Canvas(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);

        this.lblStatus = new Label("Yön: Doğu");
        this.lblBattery = new Label("Batarya: %100.0");
        this.lblDustbin = new Label("Hazne: %0.0");
        this.lblStats = new Label("Temizlenen: 0 | Kalan: 0");

        this.cmbDirtType = new ComboBox<>();
        this.cmbDirtType.getItems().addAll("Toz", "Sıvı", "Leke");
        this.cmbDirtType.setValue("Toz");
        this.cmbAlgorithm = new ComboBox<>();
        this.cmbAlgorithm.getItems().addAll("Rastgele", "Spiral", "Duvar Takip", "Akıllı Rota");
        this.cmbAlgorithm.setValue("Akıllı Rota");

        this.sldSpeed = new Slider(1, 5, 1);

        setupUI();
        this.setStyle("-fx-background-color: " + BG_COLOR + ";");
    }

    private void setupUI() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: " + PANEL_COLOR + "; -fx-background-radius: 15; -fx-border-radius: 15;");

        BorderPane.setMargin(sidebar, new Insets(15));
        BorderPane.setMargin(canvas, new Insets(15, 15, 15, 0));

        canvas.setOnMouseClicked(e -> {
            int x = (int) (e.getX() / CELL_SIZE);
            int y = (int) (e.getY() / CELL_SIZE);
            if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
                if (e.isShiftDown()) controller.addObstacle(x, y);
                else {
                    String selected = cmbDirtType.getValue();
                    DirtType type = DirtType.DUST;
                    if (selected.equals("Sıvı")) type = DirtType.LIQUID;
                    if (selected.equals("Leke")) type = DirtType.STAIN;
                    controller.addDirt(x, y, type);
                }
                draw();
            }
        });

        String baseBtnStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;";

        Button btnStart = new Button("▶ Başlat");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.setStyle(baseBtnStyle + "-fx-background-color: " + ACCENT_BLUE + ";");

        Button btnPause = new Button("⏸ Duraklat");
        btnPause.setMaxWidth(Double.MAX_VALUE);
        btnPause.setStyle(baseBtnStyle + "-fx-background-color: #64748B;");

        Button btnReset = new Button("⬛ Sıfırla");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setStyle(baseBtnStyle + "-fx-background-color: " + ACCENT_RED + ";");

        Button btnToStation = new Button("⌂ İstasyona Dön");
        btnToStation.setMaxWidth(Double.MAX_VALUE);
        btnToStation.setStyle(baseBtnStyle + "-fx-background-color: " + ACCENT_GREEN + ";");

        btnStart.setOnAction(e -> controller.startSimulation((int) sldSpeed.getValue(), cmbAlgorithm.getValue()));
        btnPause.setOnAction(e -> controller.pauseSimulation());
        btnReset.setOnAction(e -> { controller.resetSimulation(); draw(); });
        btnToStation.setOnAction(e -> controller.sendToStation());

        Label title1 = createTitle("Araçlar");
        Label title2 = createTitle("Temizlik Algoritması");
        Label title3 = createTitle("Robot Durumu");

        Label lblInfo = new Label("Sol Tık: Kir Ekle\nShift + Sol Tık: Mobilya Ekle");
        lblInfo.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        styleLabel(lblStatus); styleLabel(lblBattery); styleLabel(lblDustbin); styleLabel(lblStats);
        cmbDirtType.setMaxWidth(Double.MAX_VALUE); cmbAlgorithm.setMaxWidth(Double.MAX_VALUE);

        HBox controlBox = new HBox(10, btnStart, btnPause);
        controlBox.setAlignment(Pos.CENTER);

        sidebar.getChildren().addAll(
                title1, new Label("Kir Türü:"), cmbDirtType, lblInfo,
                new Separator(), title2, cmbAlgorithm, new Label("Hız (1x - 5x):"), sldSpeed,
                new Separator(), title3, lblStatus, lblBattery, lblDustbin, lblStats,
                new Separator(), controlBox, btnToStation, btnReset
        );

        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof Label && node != title1 && node != title2 && node != title3 && node != lblInfo) {
                ((Label) node).setTextFill(Color.web(TEXT_COLOR));
            }
        }

        this.setCenter(canvas);
        this.setLeft(sidebar);
    }

    private Label createTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        return lbl;
    }

    private void styleLabel(Label lbl) {
        lbl.setStyle("-fx-text-fill: " + ACCENT_GREEN + "; -fx-font-weight: bold;");
    }

    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int[][] grid = controller.getGrid();
        DirtType[][] dirtGrid = controller.getDirtGrid();
        int[][] dirtHealth = controller.getDirtHealth(); // YENİ: Sağlık matrisi

        int rx = controller.getRobotX();
        int ry = controller.getRobotY();
        int sx = controller.getStationX();
        int sy = controller.getStationY();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                gc.setFill(Color.web("#E2D5C3"));
                gc.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                gc.setStroke(Color.web("#D1C0A8"));
                gc.setLineWidth(1);
                gc.strokeRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 1) {
                    gc.setFill(Color.web("#8B5A2B"));
                    gc.fillRoundRect(i * CELL_SIZE + 2, j * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10);
                } else {
                    DirtType dt = dirtGrid[i][j];
                    if (dt == DirtType.DUST) {
                        gc.setFill(Color.web("#7A7A7A"));
                        gc.fillOval(i * CELL_SIZE + CELL_SIZE/2 - 3, j * CELL_SIZE + CELL_SIZE/2 - 3, 6, 6);
                        gc.fillOval(i * CELL_SIZE + 5, j * CELL_SIZE + 5, 4, 4);
                        gc.fillOval(i * CELL_SIZE + CELL_SIZE - 8, j * CELL_SIZE + CELL_SIZE - 10, 5, 5);
                    } else if (dt == DirtType.LIQUID) {
                        gc.setFill(Color.web("#3498DB80"));
                        gc.fillOval(i * CELL_SIZE + 5, j * CELL_SIZE + 8, CELL_SIZE - 10, CELL_SIZE - 15);
                    } else if (dt == DirtType.STAIN) {
                        int health = dirtHealth[i][j];
                        if (health >= 4) {
                            gc.setFill(Color.web("#8B000080")); // Tam ve Büyük Leke (4-5 puanlık çok inatçı leke)
                            gc.fillOval(i * CELL_SIZE + 6, j * CELL_SIZE + 6, CELL_SIZE - 12, CELL_SIZE - 12);
                        } else if (health >= 2) {
                            gc.setFill(Color.web("#8B000050")); // Yarı Silinmiş Leke (2-3 puanlık)
                            gc.fillOval(i * CELL_SIZE + 9, j * CELL_SIZE + 9, CELL_SIZE - 18, CELL_SIZE - 18);
                        } else {
                            gc.setFill(Color.web("#8B000020")); // Çok Soluk, Neredeyse Biten Leke (1 puanlık)
                            gc.fillOval(i * CELL_SIZE + 12, j * CELL_SIZE + 12, CELL_SIZE - 24, CELL_SIZE - 24);
                        }
                    }
                }
            }
        }

        gc.setFill(Color.web("#1F2937"));
        gc.fillRoundRect(sx * CELL_SIZE, sy * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
        gc.setFill(Color.web(ACCENT_GREEN));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("⚡", sx * CELL_SIZE + CELL_SIZE/2 - 7, sy * CELL_SIZE + CELL_SIZE/2 + 6);

        double cx = rx * CELL_SIZE + CELL_SIZE / 2.0;
        double cy = ry * CELL_SIZE + CELL_SIZE / 2.0;
        double rRadius = CELL_SIZE / 2.0 - 2;

        gc.setFill(Color.color(0, 0, 0, 0.4));
        gc.fillOval(cx - rRadius + 2, cy - rRadius + 2, rRadius * 2, rRadius * 2);

        gc.setFill(Color.web("#F8FAFC"));
        gc.fillOval(cx - rRadius, cy - rRadius, rRadius * 2, rRadius * 2);

        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(3);
        gc.strokeOval(cx - rRadius, cy - rRadius, rRadius * 2, rRadius * 2);

        gc.setFill(Color.web(ACCENT_BLUE));
        double dotX = cx, dotY = cy;
        String dir = controller.getRobotDirection();
        if (dir.equals("Doğu")) dotX += rRadius - 5;
        else if (dir.equals("Batı")) dotX -= rRadius - 5;
        else if (dir.equals("Güney")) dotY += rRadius - 5;
        else if (dir.equals("Kuzey")) dotY -= rRadius - 5;
        gc.fillOval(dotX - 4, dotY - 4, 8, 8);

// Durum Verilerini Güncelleme
        lblBattery.setText(String.format("Batarya: %%%.1f", controller.getRobotBattery()));
        lblDustbin.setText(String.format("Hazne: %%%.1f", controller.getRobotDustbinPercentage()));
        lblStatus.setText("Yön: " + controller.getRobotDirection());
        lblStats.setText("Temizlenen: " + controller.getCleanedCount() + " | Kalan Kir: " + controller.getTotalDirtCount());

        // YENİ: Görev tamamlandı animasyonu / görseli
        if (controller.isMissionCompleted() && controller.getRobotX() == controller.getStationX() && controller.getRobotY() == controller.getStationY()) {
            // Ekranı hafifçe karart (Odaklanma efekti)
            gc.setFill(Color.color(0, 0, 0, 0.6));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Ortaya başarı mesajını yazdır
            gc.setFill(Color.web(ACCENT_GREEN));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 34));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText("✨ GÖREV TAMAMLANDI ✨", canvas.getWidth() / 2, canvas.getHeight() / 2);

            // Çizim ayarlarını normale döndür (Diğer çizimleri bozmamak için)
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        }
    }
}