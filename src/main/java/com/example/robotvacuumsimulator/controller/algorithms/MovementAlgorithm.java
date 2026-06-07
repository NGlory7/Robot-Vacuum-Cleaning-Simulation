package com.example.robotvacuumsimulator.controller.algorithms;

import com.example.robotvacuumsimulator.controller.GameController;
import com.example.robotvacuumsimulator.model.Robot;

import java.util.List;

public interface MovementAlgorithm {
    /**
     * Robotun bir sonraki hareketini belirler ve uygular.
     *
     * @param robot   Hareket ettirilecek robot
     * @param controller Oyun kontrolcüsü (harita bilgilerine ve state'e erişim için)
     * @return Eğer robot bu adımda hedefine ulaşamadıysa (örneğin istasyona dönüş tetiklendiyse) false, aksi halde true döner.
     */
    void move(Robot robot, GameController controller);
    
    /**
     * GUI'de rotayı çizdirmek için planlanan yolu döndürür.
     * 
     * @return Planlanan rota koordinatları listesi
     */
    List<int[]> getPlannedPath();
    
    /**
     * Algoritma ismi
     */
    String getName();
}
