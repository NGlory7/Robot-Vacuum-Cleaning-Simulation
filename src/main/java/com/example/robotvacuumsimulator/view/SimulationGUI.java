package com.example.robotvacuumsimulator.view;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.model.DirtType;
import com.example.robotvacuumsimulator.model.Furniture;
import com.example.robotvacuumsimulator.model.FurnitureType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SimulationGUI extends BorderPane {
    private final Canvas canvas;
    private final GameController controller;

    private final Label lblPos, lblDir, lblBatteryText, lblBatteryWarning, lblDustbinText;
    private final ProgressBar pbBattery, pbDustbin;

    private final Label lblAreaStats, lblTime, lblDustCollected;
    private final ComboBox<String> cmbDirtType;
    private final ComboBox<FurnitureType> cmbFurnitureType;
    private final ToggleGroup algoGroup, placementGroup;
    private final Slider sldSpeed;
    private final ToggleButton btnModeDirt, btnModeFurniture;

    private final int CELL_SIZE = 40;
    private final int GRID_SIZE = 16;
    private final double TOTAL_AREA = 256.0;

    private final String BG_COLOR = "#0F172A";
    private final String PANEL_COLOR = "#1E293B";
    private final String TEXT_COLOR = "#94A3B8";
    private final String WHITE_TEXT = "#F8FAFC";
    private final String ACCENT_BLUE = "#3B82F6";
    private final String ACCENT_GREEN = "#10B981";
    private final String ACCENT_RED = "#EF4444";
    private final String ACCENT_YELLOW = "#F59E0B";

    public SimulationGUI(GameController controller) {
        this.controller = controller;
        this.canvas = new Canvas(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);

        this.lblPos = new Label("(0, 0)");
        this.lblDir = new Label("Doğu (+)");
        this.lblBatteryText = new Label("100%");
        this.lblDustbinText = new Label("0%");

        this.lblBatteryWarning = new Label("");
        this.lblBatteryWarning.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        this.pbBattery = new ProgressBar(1.0);
        this.pbBattery.setStyle("-fx-accent: " + ACCENT_YELLOW + "; -fx-control-inner-background: #334155;");
        this.pbBattery.setPrefWidth(120);

        this.pbDustbin = new ProgressBar(0.0);
        this.pbDustbin.setStyle("-fx-accent: #94A3B8; -fx-control-inner-background: #334155;");
        this.pbDustbin.setPrefWidth(120);

        this.lblAreaStats = new Label("Toplam: 256m² | Temiz: 0m² | Kalan: 256m²");
        this.lblTime = new Label("⏱ 00:00");
        this.lblDustCollected = new Label("🧹 Toz: %0");

        this.cmbDirtType = new ComboBox<>();
        this.cmbDirtType.getItems().addAll("Toz", "Sıvı", "Leke");
        this.cmbDirtType.setValue("Toz");

        this.cmbFurnitureType = new ComboBox<>();
        this.cmbFurnitureType.getItems().addAll(FurnitureType.values());
        this.cmbFurnitureType.setValue(FurnitureType.SOFA);

        this.algoGroup = new ToggleGroup();
        this.sldSpeed = new Slider(1, 5, 1);

        placementGroup = new ToggleGroup();
        btnModeDirt = new ToggleButton("❄ Kir Ekle");
        btnModeFurniture = new ToggleButton("🛋 Eşya Ekle");
        btnModeDirt.setToggleGroup(placementGroup);
        btnModeFurniture.setToggleGroup(placementGroup);
        btnModeDirt.setSelected(true);

        setupUI();
        this.setStyle("-fx-background-color: " + BG_COLOR + ";");
    }

    private void setupUI() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(300);
        sidebar.setStyle("-fx-background-color: " + PANEL_COLOR + "; -fx-border-color: #334155; -fx-border-width: 0 1 0 0;");

        HBox modeBox = new HBox(5, btnModeDirt, btnModeFurniture);
        modeBox.setAlignment(Pos.CENTER);
        btnModeDirt.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnModeDirt, Priority.ALWAYS);
        btnModeFurniture.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnModeFurniture, Priority.ALWAYS);

        String tglStyle = "-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 12px;";
        btnModeDirt.setStyle(tglStyle);
        btnModeFurniture.setStyle(tglStyle);

        placementGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            btnModeDirt.setStyle(tglStyle);
            btnModeFurniture.setStyle(tglStyle);

            if (newVal == btnModeDirt) btnModeDirt.setStyle(tglStyle + "-fx-background-color: " + ACCENT_BLUE + ";");
            else if (newVal == btnModeFurniture) btnModeFurniture.setStyle(tglStyle + "-fx-background-color: " + ACCENT_GREEN + ";");
        });
        btnModeDirt.setStyle(tglStyle + "-fx-background-color: " + ACCENT_BLUE + ";");

        VBox algoBox = new VBox(8);
        String[] algos = {"Akıllı Rota", "Spiral", "Duvar Takip", "Rastgele"};
        for (String algo : algos) {
            RadioButton rb = new RadioButton(algo);
            rb.setToggleGroup(algoGroup);
            rb.setStyle("-fx-text-fill: " + TEXT_COLOR + ";");
            if (algo.equals("Akıllı Rota")) rb.setSelected(true);
            algoBox.getChildren().add(rb);
        }

        GridPane statusGrid = new GridPane();
        statusGrid.setVgap(10);
        statusGrid.setHgap(15);
        Label lbl1 = createSubTitle("Konum"); Label lbl2 = createSubTitle("Yön");
        Label lbl3 = createSubTitle("Batarya"); Label lbl4 = createSubTitle("Hazne");

        styleWhite(lblPos); styleWhite(lblDir); styleWhite(lblBatteryText); styleWhite(lblDustbinText);

        VBox batteryVBox = new VBox(2, new HBox(8, pbBattery, lblBatteryText), lblBatteryWarning);
        HBox dustbinBox = new HBox(8, pbDustbin, lblDustbinText);
        dustbinBox.setAlignment(Pos.CENTER_LEFT);

        statusGrid.add(lbl1, 0, 0); statusGrid.add(lblPos, 1, 0);
        statusGrid.add(lbl2, 0, 1); statusGrid.add(lblDir, 1, 1);
        statusGrid.add(lbl3, 0, 2); statusGrid.add(batteryVBox, 0, 3, 2, 1);
        statusGrid.add(lbl4, 0, 4); statusGrid.add(dustbinBox, 0, 5, 2, 1);

        String baseBtnStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10; -fx-cursor: hand;";
        Button btnStart = new Button("▶ Başlat");
        btnStart.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnStart, Priority.ALWAYS);
        btnStart.setStyle(baseBtnStyle + "-fx-background-color: " + ACCENT_BLUE + ";");

        Button btnPause = new Button("⏸ Duraklat");
        btnPause.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnPause, Priority.ALWAYS);
        btnPause.setStyle(baseBtnStyle + "-fx-background-color: #475569;");

        Button btnReset = new Button("⬛ Sıfırla");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setStyle(baseBtnStyle + "-fx-background-color: " + ACCENT_RED + ";");

        Button btnToStation = new Button("⌂ İstasyona Dön");
        btnToStation.setMaxWidth(Double.MAX_VALUE);
        btnToStation.setStyle(baseBtnStyle + "-fx-background-color: #334155;");

        btnStart.setOnAction(e -> {
            RadioButton selectedRb = (RadioButton) algoGroup.getSelectedToggle();
            controller.startSimulation((int) sldSpeed.getValue(), selectedRb.getText());
        });
        btnPause.setOnAction(e -> controller.pauseSimulation());
        btnReset.setOnAction(e -> { controller.resetSimulation(); draw(); });
        btnToStation.setOnAction(e -> controller.sendToStation());

        cmbDirtType.setMaxWidth(Double.MAX_VALUE);
        cmbFurnitureType.setMaxWidth(Double.MAX_VALUE);
        HBox controlBox = new HBox(10, btnStart, btnPause);

        Label lblHint = new Label("💡 Silmek için SAĞ TIKLA");
        lblHint.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 11px;");

        sidebar.getChildren().addAll(
                createTitle("🛠 Araçlar"), modeBox,
                createSubTitle("Kir Türü"), cmbDirtType,
                createSubTitle("Eşya (Mobilya veya Halı)"), cmbFurnitureType, // İsim Güncellendi
                lblHint,
                new Separator(),
                createTitle("⚙ Ayarlar"), createSubTitle("Robot Hızı"), sldSpeed,
                createSubTitle("Temizlik Algoritması"), algoBox,
                new Separator(),
                createTitle("🤖 Robot Durumu"), statusGrid,
                new Separator(),
                createTitle("🎮 Kontroller"), controlBox, btnReset, btnToStation
        );

        HBox footer = new HBox(40);
        footer.setPadding(new Insets(15, 20, 15, 20));
        footer.setStyle("-fx-background-color: " + PANEL_COLOR + "; -fx-border-color: #334155; -fx-border-width: 1 0 0 0;");
        footer.setAlignment(Pos.CENTER);

        lblAreaStats.setStyle("-fx-text-fill: " + WHITE_TEXT + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblTime.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        lblDustCollected.setStyle("-fx-text-fill: " + ACCENT_YELLOW + "; -fx-font-weight: bold;");
        footer.getChildren().addAll(lblAreaStats, lblTime, lblDustCollected);

        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setPadding(new Insets(20));
        canvasContainer.setStyle("-fx-background-color: #0F172A;");

        canvas.setOnMouseClicked(e -> {
            int x = (int) (e.getX() / CELL_SIZE);
            int y = (int) (e.getY() / CELL_SIZE);
            if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
                if (e.getButton() == MouseButton.SECONDARY) {
                    controller.removeEntity(x, y);
                }
                else if (e.getButton() == MouseButton.PRIMARY) {
                    if (btnModeFurniture.isSelected()) {
                        controller.addFurniture(x, y, cmbFurnitureType.getValue());
                    } else {
                        String selected = cmbDirtType.getValue();
                        DirtType type = DirtType.DUST;
                        if (selected.equals("Sıvı")) type = DirtType.LIQUID;
                        if (selected.equals("Leke")) type = DirtType.STAIN;
                        controller.addDirt(x, y, type);
                    }
                }
                draw();
            }
        });

        this.setCenter(canvasContainer);
        this.setLeft(sidebar);
        this.setBottom(footer);
    }

    private Label createTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        return lbl;
    }

    private Label createSubTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-size: 12px;");
        return lbl;
    }

    private void styleWhite(Label lbl) {
        lbl.setStyle("-fx-text-fill: " + WHITE_TEXT + "; -fx-font-weight: bold;");
    }

    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#E2D5C3"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(Color.web("#D1C0A8"));
        gc.setLineWidth(1);
        for (int i = 0; i <= GRID_SIZE; i++) {
            gc.strokeLine(i * CELL_SIZE, 0, i * CELL_SIZE, canvas.getHeight());
            gc.strokeLine(0, i * CELL_SIZE, canvas.getWidth(), i * CELL_SIZE);
        }

        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(12);
        gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int[][] grid = controller.getGrid();
        DirtType[][] dirtGrid = controller.getDirtGrid();
        int[][] dirtHealth = controller.getDirtHealth();

        // --- 1. ÖNCE HALILARI ÇİZ (Zemin Üstü) ---
        for (Furniture f : controller.getFurnitureList()) {
            if (f.getType().name().startsWith("CARPET")) {
                double px = f.getX() * CELL_SIZE;
                double py = f.getY() * CELL_SIZE;
                double pw = f.getType().getWidth() * CELL_SIZE;
                double ph = f.getType().getHeight() * CELL_SIZE;

                gc.setFill(Color.web("#CD853F"));
                gc.fillRoundRect(px + 4, py + 4, pw - 8, ph - 8, 15, 15);
                gc.setStroke(Color.web("#8B4513"));
                gc.setLineWidth(2);
                gc.strokeRoundRect(px + 10, py + 10, pw - 20, ph - 20, 8, 8);
            }
        }

        // --- 2. SONRA KİRLERİ ÇİZ (Halı Üstü) ---
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                DirtType dt = dirtGrid[i][j];
                if (dt == DirtType.DUST) {
                    gc.setFill(Color.web("#7A7A7A"));
                    gc.fillOval(i * CELL_SIZE + 10, j * CELL_SIZE + 10, 5, 5);
                    gc.fillOval(i * CELL_SIZE + 25, j * CELL_SIZE + 15, 4, 4);
                    gc.fillOval(i * CELL_SIZE + 15, j * CELL_SIZE + 25, 6, 6);
                } else if (dt == DirtType.LIQUID) {
                    gc.setFill(Color.web("#3498DB80"));
                    gc.fillOval(i * CELL_SIZE + 8, j * CELL_SIZE + 10, CELL_SIZE - 16, CELL_SIZE - 20);
                } else if (dt == DirtType.STAIN) {
                    int health = dirtHealth[i][j];
                    if (health >= 4) {
                        gc.setFill(Color.web("#8B000090"));
                        gc.fillOval(i * CELL_SIZE + 5, j * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                    } else if (health >= 2) {
                        gc.setFill(Color.web("#8B000060"));
                        gc.fillOval(i * CELL_SIZE + 10, j * CELL_SIZE + 10, CELL_SIZE - 20, CELL_SIZE - 20);
                    } else {
                        gc.setFill(Color.web("#8B000030"));
                        gc.fillOval(i * CELL_SIZE + 15, j * CELL_SIZE + 15, CELL_SIZE - 30, CELL_SIZE - 30);
                    }
                }
            }
        }

        // --- 3. EN SON MOBİLYALARI ÇİZ (Kir ve Halıların Üstüne) ---
        for (Furniture f : controller.getFurnitureList()) {
            if (f.getType().name().startsWith("CARPET")) continue; // Halıları zaten çizdik

            double px = f.getX() * CELL_SIZE;
            double py = f.getY() * CELL_SIZE;
            double pw = f.getType().getWidth() * CELL_SIZE;
            double ph = f.getType().getHeight() * CELL_SIZE;

            gc.setFill(Color.color(0,0,0,0.3));
            gc.fillRoundRect(px + 4, py + 4, pw, ph, 10, 10);

            switch (f.getType()) {
                case PLANT:
                    gc.setFill(Color.web("#A0522D"));
                    gc.fillOval(px + 10, py + 10, 20, 20);
                    gc.setFill(Color.web("#22C55E"));
                    gc.fillOval(px + 4, py + 4, 32, 32);
                    break;
                case ARMCHAIR:
                    gc.setFill(Color.web("#D4A373"));
                    gc.fillRoundRect(px + 5, py + 5, pw - 10, ph - 10, 15, 15);
                    gc.setFill(Color.web("#FAEDCD"));
                    gc.fillRoundRect(px + 15, py + 15, pw - 30, ph - 30, 8, 8);
                    break;
                case SOFA:
                    gc.setFill(Color.web("#CCD5AE"));
                    gc.fillRoundRect(px + 5, py + 5, pw - 10, ph - 10, 15, 15);
                    gc.setFill(Color.web("#E9EDC9"));
                    gc.fillRoundRect(px + 10, py + 10, 20, ph - 20, 5, 5);
                    gc.fillRoundRect(px + pw - 30, py + 10, 20, ph - 20, 5, 5);
                    break;
                case TABLE:
                    gc.setFill(Color.web("#8B5A2B"));
                    gc.fillRoundRect(px + 5, py + 5, pw - 10, ph - 10, 10, 10);
                    gc.setFill(Color.web("#A0522D"));
                    gc.fillOval(px + pw/2 - 15, py + ph/2 - 15, 30, 30);
                    break;
                case TV_UNIT:
                    gc.setFill(Color.web("#475569"));
                    gc.fillRoundRect(px + 5, py + 5, pw - 10, ph - 10, 5, 5);
                    gc.setFill(Color.web("#0F172A"));
                    gc.fillRect(px + 10, py + CELL_SIZE, pw - 20, CELL_SIZE * 2);
                    break;
            }
        }

        // --- 4. İSTASYON VE ROBOTU ÇİZ ---
        int sx = controller.getStationX();
        int sy = controller.getStationY();
        gc.setFill(Color.web("#1E293B"));
        gc.fillRoundRect(sx * CELL_SIZE + 2, sy * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10);
        gc.setFill(Color.web(ACCENT_GREEN));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.fillText("⚡", sx * CELL_SIZE + 9, sy * CELL_SIZE + 28);

        int rx = controller.getRobotX();
        int ry = controller.getRobotY();
        double cx = rx * CELL_SIZE + CELL_SIZE / 2.0;
        double cy = ry * CELL_SIZE + CELL_SIZE / 2.0;
        double rRadius = CELL_SIZE / 2.0 - 4;

        gc.setFill(Color.color(0, 0, 0, 0.4));
        gc.fillOval(cx - rRadius + 3, cy - rRadius + 3, rRadius * 2, rRadius * 2);
        gc.setFill(Color.web("#F8FAFC"));
        gc.fillOval(cx - rRadius, cy - rRadius, rRadius * 2, rRadius * 2);
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(4);
        gc.strokeOval(cx - rRadius, cy - rRadius, rRadius * 2, rRadius * 2);

        gc.setFill(Color.web(ACCENT_BLUE));
        double dotX = cx, dotY = cy;
        String dir = controller.getRobotDirection();
        if (dir.equals("Doğu")) dotX += rRadius - 6;
        else if (dir.equals("Batı")) dotX -= rRadius - 6;
        else if (dir.equals("Güney")) dotY += rRadius - 6;
        else if (dir.equals("Kuzey")) dotY -= rRadius - 6;
        gc.fillOval(dotX - 5, dotY - 5, 10, 10);

        // --- GÖRSEL ARAYÜZ (GUI) VERİ GÜNCELLEMESİ ---
        lblPos.setText("(" + rx + ", " + ry + ")");
        lblDir.setText(dir);

        double bat = controller.getRobotBattery();
        pbBattery.setProgress(bat / 100.0);
        lblBatteryText.setText(String.format("%%%d", (int)bat));

        if (controller.isCharging()) {
            lblDir.setText("İstasyonda Bekliyor");
            lblBatteryWarning.setText("⚡ Şarj Ediliyor...");
            lblBatteryWarning.setStyle("-fx-text-fill: " + ACCENT_GREEN + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            pbBattery.setStyle("-fx-accent: " + ACCENT_GREEN + "; -fx-control-inner-background: #334155;");
        }
        else if (controller.isOnCarpet()) {
            lblBatteryWarning.setText("🔥 Halı Üzerinde (Yüksek Güç)");
            lblBatteryWarning.setStyle("-fx-text-fill: " + ACCENT_RED + "; -fx-font-weight: bold; -fx-font-size: 11px;");
            pbBattery.setStyle("-fx-accent: " + ACCENT_RED + "; -fx-control-inner-background: #334155;");
        }
        else {
            lblBatteryWarning.setText("");
            if (bat < 20) pbBattery.setStyle("-fx-accent: " + ACCENT_RED + "; -fx-control-inner-background: #334155;");
            else pbBattery.setStyle("-fx-accent: " + ACCENT_YELLOW + "; -fx-control-inner-background: #334155;");
        }

        double dust = controller.getRobotDustbinPercentage();
        pbDustbin.setProgress(dust / 100.0);
        lblDustbinText.setText(String.format("%%%d", (int)dust));
        if (dust >= 100) pbDustbin.setStyle("-fx-accent: " + ACCENT_RED + "; -fx-control-inner-background: #334155;");
        else pbDustbin.setStyle("-fx-accent: #94A3B8; -fx-control-inner-background: #334155;");

        lblDustCollected.setText(String.format("🧹 Toplanan Toz: %%%d", (int)dust));

        double cleaned = controller.getCleanedCount();
        double remaining = controller.getTotalDirtCount();
        double percentage = (cleaned + remaining) == 0 ? 0 : (cleaned / (cleaned + remaining)) * 100.0;

        lblAreaStats.setText(String.format("🟢 Temiz: %.0fm² (%%%d)   |   🔴 Kalan Kir: %.0f Birim", cleaned, (int)percentage, remaining));

        int totalSecs = controller.getElapsedSeconds();
        int mins = totalSecs / 60;
        int secs = totalSecs % 60;
        lblTime.setText(String.format("⏱ %02d:%02d", mins, secs));

        if (controller.isMissionCompleted() && rx == sx && ry == sy) {
            gc.setFill(Color.color(0, 0, 0, 0.7));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.web(ACCENT_GREEN));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText("✨ GÖREV TAMAMLANDI ✨", canvas.getWidth() / 2, canvas.getHeight() / 2);
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        }
    }
}