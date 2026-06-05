module com.example.robotvacuumsimulator {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.robotvacuumsimulator to javafx.fxml;
    exports com.example.robotvacuumsimulator;
}